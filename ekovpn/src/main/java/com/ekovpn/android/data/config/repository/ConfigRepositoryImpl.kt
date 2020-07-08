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
import com.ekovpn.android.cache.settings.SettingsPrefManager
import com.ekovpn.android.data.config.*
import com.ekovpn.android.data.config.VPNServer.IkeV2Server.Companion.toServerCacheModel
import com.ekovpn.android.data.config.VPNServer.OVPNServer.Companion.toServerCacheModel
import com.ekovpn.android.data.config.downloader.FileDownloader
import com.ekovpn.android.data.config.importer.Ikev2ProfileImporter
import com.ekovpn.android.data.config.importer.OVPNProfileImporter
import com.ekovpn.android.models.Protocol
import com.google.gson.Gson
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.strongswan.android.data.VpnProfileDataSource
import java.io.File
import javax.inject.Inject


@ExperimentalCoroutinesApi
class ConfigRepositoryImpl @Inject constructor(private val context: Context,
                                               private val locationsDao: LocationsDao,
                                               private val serversDao: ServersDao,
                                               private val vpnProfileDataSource: VpnProfileDataSource,
                                               private val fileDownloader: FileDownloader,
                                               private val profileManager: ProfileManager,
                                               private val ikev2ProfileImporter: Ikev2ProfileImporter,
                                               private val ovpnProfileImporter: OVPNProfileImporter,
                                               private val settingsPrefManager: SettingsPrefManager) : ConfigRepository {

    private fun loadFromJson() = context.assets.open(FILE_NAME).bufferedReader().use {
        it.readText()
    }

    override fun hasConfiguredServers(): Boolean {
        return settingsPrefManager.getHasCompletedSetup()
    }


    private fun loadJSonData(): Array<ServerConfig> {
        val data = loadFromJson()
        return Gson().fromJson(data, Array<ServerConfig>::class.java)
    }


    override fun configureIkeV2Servers(serverConfig: Array<ServerConfig>): Flow<Result<Unit>> {
        serverConfig.forEach {
            Log.d(ConfigRepositoryImpl::class.java.simpleName, "IKEV2 ${it.serverLocation}")
        }

        val pemConfigs: MutableList<Flow<Result<ServerSetUp>>> = mutableListOf()

        serverConfig.forEach { server ->
            pemConfigs.add(fileDownloader.downloadConfigFile(server.serverLocation, Protocol.IKEV2, server.ikev2.certificate_url, server.ikev2))
        }

        return pemConfigs.merge()
                .takeWhile {
                    !it.getOrNull()?.serverLocation_?.city.equals("ZZ", true)
                }.filterNot {
                    it.getOrNull()?.serverLocation_?.city.equals("ZZ", true)
                }
                .flatMapConcat {
                    if (it.isSuccess) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Configuring ikeV2: ${it.getOrNull()}")
                        val setUp = it.getOrNull() as ServerSetUp.IkeV2Setup
                        ikev2ProfileImporter.importServerConfig(setUp.pemFileDir.toUri(), setUp.serverLocation, setUp.ikeV2)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed to configure ikeV2: ${it.getOrNull()}")
                        flowOf(Result.failure(Exception("An error occurred for config ${it.getOrNull()}")))
                    }
                }.map { profile ->
                    if (profile.getOrNull() is VPNServer) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saving ikeV2: ${profile.getOrNull()}")
                        saveIkeV2Profile(profile.getOrNull() as VPNServer.IkeV2Server)
                        Result.success(Unit)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed Saving ikeV2: ${profile.getOrNull()}")
                        Result.failure(Exception("An error occurred for config"))
                    }
                }
    }

    override fun configureOVPNServers(serverConfig: Array<ServerConfig>): Flow<Result<Unit>> {
        serverConfig.forEach {
            Log.d(ConfigRepositoryImpl::class.java.simpleName, "OPEN VPN ${it.serverLocation}")
        }

        val ovpnConfigs: MutableList<Flow<Result<ServerSetUp>>> = mutableListOf()

        serverConfig.forEach { server ->
            server.open_vpn.forEach {
                ovpnConfigs.add(fileDownloader.downloadConfigFile(server.serverLocation, Protocol.fromString(it.protocol), it.configfileurl))
            }
        }

        return ovpnConfigs.merge()
                .takeWhile {
                    !it.getOrNull()?.serverLocation_?.city.equals("ZZ", true)
                }.filterNot {
                    it.getOrNull()?.serverLocation_?.city.equals("ZZ", true)
                }
                .flatMapConcat {
                    if (it.isSuccess) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Configuring OVPN: ${it.getOrNull()}")
                        configureOVPNProfileForServer(it.getOrNull() as ServerSetUp.OVPNSetup)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed to configure OVPN: ${it.getOrNull()}")
                        flowOf(Result.failure(Exception("An error occurred for config ${it.getOrNull()}")))
                    }
                }.map { profile ->
                    if (profile.getOrNull() is VPNServer) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saving OVPN: ${profile.getOrNull()}")
                        saveOVPNProfile(profile.getOrNull() as VPNServer.OVPNServer)
                        Result.success(Unit)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed Saving OVPN: ${profile.getOrNull()}")
                        Result.failure(Exception("An error occurred for config"))
                    }
                }
    }


    override fun fetchAndConfigureServers(): Flow<Result<Unit>> {
        val configList = loadJSonData()
        configList.sortBy { it.serverLocation.city }

        val setOfLocations = mutableSetOf<ServerLocation>()

        configList.forEach {
            setOfLocations.add(it.serverLocation)
        }

        val listOfCachedLocations = setOfLocations.map {
            ServerLocation.toLocationCacheModel(it)
        }.toList()

        return listOf(configureOVPNServers(configList), configureIkeV2Servers(configList)).merge()
                .onStart {
                    locationsDao.insert(listOfCachedLocations)
                }
                .onCompletion {
            //settingsPrefManager.setHasCompletedSetup()
        }
    }

    private suspend fun saveIkeV2Profile(result: VPNServer.IkeV2Server) {
        vpnProfileDataSource.insertProfile(result.ikeV2Profile)
        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        location?.let {
            val serCacheModel = result.toServerCacheModel(location)
            serversDao.insert(serCacheModel)
        }

        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saved: $result")
    }

    private suspend fun saveOVPNProfile(result: VPNServer.OVPNServer) {
        val profile = result.openVpnProfile
        profile.mName = "${result.serverLocation.city}-${result.serverLocation.country}-${result.protocol.value}"
        profileManager.addProfile(profile)
        profileManager.saveProfile(context, profile)
        profileManager.saveProfileList(context)

        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        location?.let {
            val serCacheModel = result.toServerCacheModel(location, result.protocol)
            serversDao.insert(serCacheModel)
        }

        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saved: $result")
    }

    private fun configureOVPNProfileForServer(serverSetUp: ServerSetUp.OVPNSetup): Flow<Result<VPNServer>> {
        return ovpnProfileImporter.importServerConfig("file://${serverSetUp.ovpnFileDir}".toUri()).map {
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