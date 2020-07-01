/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.ekovpn.android.cache.room.dao.LocationsDao
import com.ekovpn.android.cache.room.dao.ServersDao
import com.ekovpn.android.cache.room.entities.LocationCacheModel
import com.ekovpn.android.cache.room.entities.ServerCacheModel
import com.ekovpn.android.cache.settings.SettingsPrefManager
import com.ekovpn.android.data.config.downloader.FileDownloader
import com.ekovpn.android.data.config.importer.OVPNProfileImporter
import com.ekovpn.android.data.config.VPNServer
import com.ekovpn.android.data.config.ServerConfig
import com.ekovpn.android.data.config.ServerLocation
import com.ekovpn.android.data.config.ServerSetUp
import com.ekovpn.android.models.Protocol
import com.google.gson.Gson
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject


class ConfigRepositoryImpl @Inject constructor(private val context: Context,
                                               private val locationsDao: LocationsDao,
                                               private val serversDao: ServersDao,
                                               private val fileDownloader: FileDownloader,
                                               private val profileManager: ProfileManager,
                                               private val profileImporter: OVPNProfileImporter,
                                               private val settingsPrefManager: SettingsPrefManager) : ConfigRepository {

    private fun loadFromJson() = context.assets.open(FILE_NAME).bufferedReader().use {
        it.readText()
    }

    override fun hasConfiguredServers(): Boolean {
        return settingsPrefManager.getHasCompletedSetup()
    }

    override fun fetchAndConfigureServers(): Flow<Result<Unit>> {
        val data = loadFromJson()
        val gson = Gson()
        val serverConfig = gson.fromJson(data, Array<ServerConfig>::class.java)
        serverConfig.forEach {
            Log.d(ConfigRepositoryImpl::class.java.simpleName, it.serverLocation.toString())
        }

        val setOfLocations = mutableSetOf<ServerLocation>()

        serverConfig.sortBy { it.serverLocation.city }
        val ovpnConfigs: MutableList<Flow<Result<ServerSetUp>>> = mutableListOf()

        serverConfig.forEach { server ->
            server.open_vpn.forEach {
                setOfLocations.add(server.serverLocation)
                ovpnConfigs.add(fileDownloader.downloadConfigFile(server.serverLocation, Protocol.fromString(it.protocol), it.configfileurl))
            }
        }

        val listOfCachedLocations = setOfLocations.map {
            ServerLocation.toLocationCacheModel(it)
        }.toList()

        return ovpnConfigs.merge()
                .onStart {
                    locationsDao.insert(listOfCachedLocations)
                }
                .takeWhile {
                    !it.getOrNull()?.serverLocation_?.city.equals("ZZ", true)
                }.filterNot {
                    it.getOrNull()?.serverLocation_?.city.equals("ZZ", true)
                }
                .flatMapConcat {
                    if (it.isSuccess) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Configuring: ${it.getOrNull()}")
                        configureOVPNProfileForServer(it.getOrNull() as ServerSetUp.OVPNSetup)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed to configure: ${it.getOrNull()}")
                        flowOf(Result.failure(Exception("An error occurred for config ${it.getOrNull()}")))
                    }
                }.map { profile ->
                    if (profile.getOrNull() is VPNServer) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saving: ${profile.getOrNull()}")
                        userActionSaveProfile(profile.getOrNull() as VPNServer.OVPNServer)
                        Result.success(Unit)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed Saving: ${profile.getOrNull()}")
                        Result.failure(Exception("An error occurred for config"))
                    }
                }.onCompletion {
                    settingsPrefManager.setHasCompletedSetup()
                }
    }

    private suspend fun userActionSaveProfile(result: VPNServer.OVPNServer) {
        val profile = result.openVpnProfile
        profile.mName = "${result.serverLocation.city}-${result.serverLocation.country}-${result.protocol.value}"
        profileManager.addProfile(profile)
        profileManager.saveProfile(context, profile)
        profileManager.saveProfileList(context)

        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        location?.let {
            val serCacheModel = VPNServer.OVPNServer.toServerCacheModel(location, result.protocol, profile.uuidString)
            serversDao.insert(serCacheModel)
        }

        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saved: $result")
    }

    private fun configureOVPNProfileForServer(serverSetUp: ServerSetUp.OVPNSetup): Flow<Result<VPNServer>> {
        return profileImporter.importServerConfig("file://${serverSetUp.ovpnFileDir}".toUri()).map {
            if (it.isSuccess) {
                Log.d(ConfigRepositoryImpl::class.java.simpleName, "Importing: ${it.getOrNull()}")
                File(serverSetUp.ovpnFileDir).delete()
                Result.success(VPNServer.OVPNServer(it.getOrNull()!!, serverSetUp.serverLocation, serverSetUp.protocol))
            } else {
                Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed Importing: ${it.getOrNull()}")
                Result.failure(it.exceptionOrNull() ?: Exception("An error occurred"))
            }
        }
    }

    companion object {
        const val FILE_NAME = "servers.json"
    }

}