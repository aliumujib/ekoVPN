/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.config.repository

import android.content.Context
import android.util.Log
import com.ekovpn.android.data.cache.room.dao.LocationsDao
import com.ekovpn.android.data.cache.room.dao.ServersDao
import com.ekovpn.android.data.cache.room.entities.LocationCacheModel
import com.ekovpn.android.data.cache.settings.SettingsPrefManager
import com.ekovpn.android.data.remote.retrofit.EkoVPNApiService
import com.ekovpn.android.data.repositories.config.*
import com.ekovpn.android.data.repositories.config.ServerLocation.Companion.toLocationCacheModel
import com.google.gson.Gson
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
class ConfigRepositoryImpl @Inject constructor(private val context: Context,
                                               private val locationsDao: LocationsDao,
                                               private val serversDao: ServersDao,
                                               private val ekoVPNAPIService: EkoVPNApiService,
                                               private val openVpnConfigurator: OpenVpnConfigurator,
                                               private val wireGuardConfigurator: WireGuardConfigurator,
                                               private val profileManager: ProfileManager,
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
            wireGuardConfigurator.deleteAllTunnels()
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }


    private fun loadJSonData(): Array<ServerConfig> {
        val data = loadFromJson()
        return Gson().fromJson(data, Array<ServerConfig>::class.java)
    }


    override fun fetchAndConfigureServers(): Flow<Result<Unit>> {
        return flow {
            val serverConfigurations = ekoVPNAPIService.fetchServerConfig().data
            emit(serverConfigurations!!)
        }.map { serverConfigurations ->
            serverConfigurations?.sortedBy { it.serverLocation.city }

            val setOfLocations = mutableSetOf<ServerLocation>()

            serverConfigurations?.forEach {
                setOfLocations.add(it.serverLocation)
            }

            Log.d(ConfigRepositoryImpl::class.java.simpleName, serverConfigurations.toString())

            val listOfCachedLocations = setOfLocations.mapIndexed { index, serverLocation ->
                serverLocation.toLocationCacheModel(index + 1)
            }.toList()

            locationsDao.insert(listOfCachedLocations)

            listOf(openVpnConfigurator.configureOVPNServers(serverConfigurations),
                    iKev2CertificateImporter.configureIkeV2Servers(serverConfigurations),
                    wireGuardConfigurator.configureWireGuardServers(serverConfigurations))
        }.onStart {
            wireGuardConfigurator.deleteAllTunnels()
            locationsDao.deleteAll()
            serversDao.deleteAll()
            //profileManager.deleteAllProfiles(context)
        }.flatMapMerge {
            it.merge()
        }.catch {
            it.printStackTrace()
            wireGuardConfigurator.deleteAllTunnels()
            throw it
        }.onCompletion {
            if (it == null) {
                markSetupAsComplete()
            }
        }.map {
            Log.d(ConfigRepositoryImpl::class.java.simpleName, "List $it")
            Result.success(Unit)
        }.take(3).flowOn(Dispatchers.IO)
    }

    override fun markSetupAsComplete() {
        settingsPrefManager.setHasCompletedSetup(true)
    }

    companion object {
        const val FILE_NAME = "servers_wg_final.json"
    }

}