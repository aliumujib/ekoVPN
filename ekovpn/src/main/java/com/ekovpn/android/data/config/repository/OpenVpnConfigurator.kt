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
import com.ekovpn.android.data.config.ServerLocation
import com.ekovpn.android.data.config.ServerSetUp
import com.ekovpn.android.data.config.VPNServer
import com.ekovpn.android.data.config.VPNServer.OVPNServer.Companion.toServerCacheModel
import com.ekovpn.android.data.config.downloader.FileDownloader
import com.ekovpn.android.data.config.importer.OVPNProfileImporter
import com.ekovpn.android.models.Protocol
import com.ekovpn.android.utils.ext.runOnAll
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.io.File
import java.lang.IllegalStateException
import javax.inject.Inject

@ExperimentalCoroutinesApi
class OpenVpnConfigurator @Inject constructor(
        private val context: Context,
        private val locationsDao: LocationsDao,
        private val fileDownloader: FileDownloader,
        private val profileManager: ProfileManager,
        private val ovpnProfileImporter: OVPNProfileImporter,
        private val serversDao: ServersDao) {

    fun configureOVPNServers(serverConfigurations: Array<ServerConfig>): Flow<List<ServerCacheModel>> {
        serverConfigurations.forEach {
            Log.d(OpenVpnConfigurator::class.java.simpleName, "OPEN VPN ${it.serverLocation}")
        }

        val listOfConfigurationParameters: MutableList<Triple<ServerLocation, Protocol, String>> = mutableListOf()

        serverConfigurations.forEach { server ->
            server.open_vpn.forEach {
                listOfConfigurationParameters.add(Triple(server.serverLocation, Protocol.fromString(it.protocol), it.configfileurl))
            }
        }

        return flowOf(listOfConfigurationParameters).flatMapLatest { ids -> runOnAll(ids.toSet(), ::configureOVPNServer as (Triple<ServerLocation, Protocol, String>) -> Flow<ServerCacheModel>) }
                .map { m -> m.values.toList() }
    }

    private fun configureOVPNServer(configDetails: Triple<ServerLocation, Protocol, String>): Flow<ServerCacheModel> {

        Log.d(OpenVpnConfigurator::class.java.simpleName, "OPEN VPN ${configDetails.first}")

        return fileDownloader.downloadOVPNConfig(configDetails.first, configDetails.second, configDetails.third)
                .map {
                    if (it.isSuccess) {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Configuring OVPN: ${it.getOrNull()}")
                        configureOVPNProfileForServer(it.getOrNull() as ServerSetUp.OVPNSetup)
                    } else {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Failed to configure OVPN: ${it.getOrNull()}")
                    }
                }.map { server ->
                    if (server is VPNServer) {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Saving OVPN: $server")
                        val serverCacheModel = saveOVPNProfile(server as VPNServer.OVPNServer)
                        serverCacheModel!!
                    } else {
                        Log.d(OpenVpnConfigurator::class.java.simpleName, "Failed Saving OVPN: $server")
                        throw Exception("An error occurred for config ${configDetails.first}")
                    }
                }
    }

    private fun configureOVPNProfileForServer(serverSetUp: ServerSetUp.OVPNSetup): VPNServer {
        return ovpnProfileImporter.importServerConfig("file://${serverSetUp.ovpnFileDir}".toUri()).let {
            File(serverSetUp.ovpnFileDir).delete()
            VPNServer.OVPNServer(it, serverSetUp.serverLocation, serverSetUp.protocol)
        }
    }

    private suspend fun saveOVPNProfile(result: VPNServer.OVPNServer): ServerCacheModel? {
        val profile = result.openVpnProfile
        profile.mName = "${result.serverLocation.city}-${result.serverLocation.country}-${result.protocol.value}"
        profileManager.addProfile(profile)
        profileManager.saveProfile(context, profile)
        profileManager.saveProfileList(context)
        var serverCacheModel: ServerCacheModel? = null
        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        location?.let {
            serverCacheModel = result.toServerCacheModel(location, result.protocol)
            serverCacheModel?.let {
                serversDao.insert(it)
            } ?: throw IllegalStateException("Error saving null OVPN profile $result")
            Log.d(OpenVpnConfigurator::class.java.simpleName, "Saved: $result")
        } ?: throw IllegalStateException("Error saving null OVPN location $location")
        return serverCacheModel
    }
}