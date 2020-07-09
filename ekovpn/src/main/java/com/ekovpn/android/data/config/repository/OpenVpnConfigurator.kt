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
import com.ekovpn.android.cache.room.entities.ServerCacheModel
import com.ekovpn.android.data.config.ServerConfig
import com.ekovpn.android.data.config.ServerSetUp
import com.ekovpn.android.data.config.VPNServer
import com.ekovpn.android.data.config.VPNServer.OVPNServer.Companion.toServerCacheModel
import com.ekovpn.android.data.config.downloader.FileDownloader
import com.ekovpn.android.data.config.importer.OVPNProfileImporter
import com.ekovpn.android.models.Protocol
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

@ExperimentalCoroutinesApi
class OpenVpnConfigurator @Inject constructor(
        private val context: Context,
        private val locationsDao: LocationsDao,
        private val fileDownloader: FileDownloader,
        private val profileManager: ProfileManager,
        private val ovpnProfileImporter: OVPNProfileImporter,
        private val serversDao: ServersDao) {

    fun configureOVPNServers(serverConfig: Array<ServerConfig>): Flow<Result<Unit>> {
        serverConfig.forEach {
            Log.d(OpenVpnConfigurator::class.java.simpleName, "OPEN VPN ${it.serverLocation}")
        }

        val ovpnConfigs: MutableList<Flow<Result<ServerSetUp>>> = mutableListOf()

        serverConfig.forEach { server ->
            server.open_vpn.forEach {
                ovpnConfigs.add(fileDownloader.downloadOVPNConfig(server.serverLocation, Protocol.fromString(it.protocol), it.configfileurl))
            }
        }

        return ovpnConfigs.merge()
                .flatMapConcat {
                    if (it.isSuccess) {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Configuring OVPN: ${it.getOrNull()}")
                        configureOVPNProfileForServer(it.getOrNull() as ServerSetUp.OVPNSetup)
                    } else {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Failed to configure OVPN: ${it.getOrNull()}")
                        flowOf(Result.failure(Exception("An error occurred for config ${it.getOrNull()}")))
                    }
                }.map { profile ->
                    if (profile.getOrNull() is VPNServer) {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Saving OVPN: ${profile.getOrNull()}")
                        saveOVPNProfile(profile.getOrNull() as VPNServer.OVPNServer)
                        Result.success(Unit)
                    } else {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Failed Saving OVPN: ${profile.getOrNull()}")
                        Result.failure(Exception("An error occurred for config"))
                    }
                }
    }

    private fun configureOVPNProfileForServer(serverSetUp: ServerSetUp.OVPNSetup): Flow<Result<VPNServer>> {
        return ovpnProfileImporter.importServerConfig("file://${serverSetUp.ovpnFileDir}".toUri()).map {
            if (it.isSuccess) {
                Log.d(OpenVpnConfigurator::class.java.simpleName, "Importing: ${it.getOrNull()}")
                File(serverSetUp.ovpnFileDir).delete()
                Result.success(VPNServer.OVPNServer(it.getOrNull()!!, serverSetUp.serverLocation, serverSetUp.protocol))
            } else {
                Log.d(OpenVpnConfigurator::class.java.simpleName, "Failed Importing: ${it.getOrNull()}")
                Result.failure(it.exceptionOrNull() ?: Exception("An error occurred"))
            }
        }
    }

    private suspend fun saveOVPNProfile(result: VPNServer.OVPNServer) {
        val profile = result.openVpnProfile
        profile.mName = "${result.serverLocation.city}-${result.serverLocation.country}-${result.protocol.value}"
        profileManager.addProfile(profile)
        profileManager.saveProfile(context, profile)
        profileManager.saveProfileList(context)
        val serCacheModel : ServerCacheModel
        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        location?.let {
            val serCacheModel = result.toServerCacheModel(location, result.protocol)
            serversDao.insert(serCacheModel)
            Log.d(OpenVpnConfigurator::class.java.simpleName, "Saved: $result")
        }
        return
    }
}