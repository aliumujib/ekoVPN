/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.config.repository

import android.content.Context
import android.util.Log
import com.ekovpn.android.data.cache.room.dao.LocationsDao
import com.ekovpn.android.data.cache.room.dao.ServersDao
import com.ekovpn.android.data.cache.settings.SettingsPrefManager
import com.ekovpn.android.data.repositories.config.*
import com.ekovpn.android.data.repositories.config.ServerLocation.Companion.toLocationCacheModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
class ConfigRepositoryImpl @Inject constructor(private val context: Context,
                                               private val locationsDao: LocationsDao,
                                               private val serversDao: ServersDao,
                                               private val openVpnConfigurator: OpenVpnConfigurator,
                                               private val wireGuardConfigurator: WireGuardConfigurator,
                                               private val iKev2CertificateImporter: IkeV2Configurator,
                                               private val settingsPrefManager: SettingsPrefManager) : ConfigRepository {

    private fun loadFromJson() = context.assets.open(FILE_NAME).bufferedReader().use {
        it.readText()
    }

    override fun hasConfiguredServers(): Boolean {
        return settingsPrefManager.getHasCompletedSetup()
    }

    override fun logOutAndClearData(): Flow<Unit> {
        return flow {
            settingsPrefManager.setHasCompletedSetup(false)
            locationsDao.deleteAll()
            serversDao.deleteAll()
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }


    private fun loadJSonData(): Array<ServerConfig> {
        val data = loadFromJson()
        return Gson().fromJson(data, Array<ServerConfig>::class.java)
    }


    override fun fetchAndConfigureServers(): Flow<Result<Unit>> {

        val serverConfigurations = loadJSonData()
        serverConfigurations.sortBy { it.serverLocation.city }

        val setOfLocations = mutableSetOf<ServerLocation>()

        serverConfigurations.forEach {
            setOfLocations.add(it.serverLocation)
        }

        Log.d(ConfigRepositoryImpl::class.java.simpleName, serverConfigurations.toString())

        val listOfCachedLocations = setOfLocations.mapIndexed { index, serverLocation ->
            serverLocation.toLocationCacheModel(index + 1)
        }.toList()

        val configurationOperations = listOf(openVpnConfigurator.configureOVPNServers(serverConfigurations),
                iKev2CertificateImporter.configureIkeV2Servers(serverConfigurations),
                wireGuardConfigurator.configureWireGuardServers(serverConfigurations))

        return configurationOperations
                .merge()
                .onStart {
                    locationsDao.deleteAll()
                    serversDao.deleteAll()
                    locationsDao.insert(listOfCachedLocations)
                }.catch {
                    it.printStackTrace()
                    throw it
                }
                .onCompletion {
                    if (it == null) {
                        settingsPrefManager.setHasCompletedSetup(true)
                    }
                }.map {
                    Log.d(ConfigRepositoryImpl::class.java.simpleName, "List $it")
                    Result.success(Unit)
                }.take(configurationOperations.size)
                .flowOn(Dispatchers.IO)
    }


    companion object {
        const val FILE_NAME = "servers_wg_edit.json"
    }

}