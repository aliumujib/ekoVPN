/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.config.repository

import android.util.Log
import androidx.core.net.toUri
import com.ekovpn.android.data.cache.room.dao.LocationsDao
import com.ekovpn.android.data.cache.room.dao.ServersDao
import com.ekovpn.android.data.cache.room.entities.ServerCacheModel
import com.ekovpn.android.data.repositories.config.ServerConfig
import com.ekovpn.android.data.repositories.config.ServerLocation
import com.ekovpn.android.data.repositories.config.ServerSetUp
import com.ekovpn.android.data.repositories.config.VPNServer
import com.ekovpn.android.data.repositories.config.VPNServer.WireGuardServer.Companion.toServerCacheModel
import com.ekovpn.android.data.repositories.config.downloader.FileDownloader
import com.ekovpn.android.data.repositories.config.importer.WireGuardConfigImporter
import com.ekovpn.android.models.Protocol
import com.ekovpn.wireguard.WireGuardInitializer
import com.ekovpn.wireguard.repo.TunnelManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

@ExperimentalCoroutinesApi
class WireGuardConfigurator @Inject constructor(
        private val locationsDao: LocationsDao,
        private val fileDownloader: FileDownloader,
        private val configImporter: WireGuardConfigImporter,
        private val serversDao: ServersDao) {

    fun deleteAllTunnels(){
        WireGuardInitializer.getTunnelManager().deleteAll()
    }

    fun configureWireGuardServers(serverConfigurations: Array<ServerConfig>): Flow<List<ServerCacheModel>> {
        serverConfigurations.forEach {
            Log.d(WireGuardConfigurator::class.java.simpleName, "WIRE_GUARD ${it.serverLocation}")
        }

        val listOfConfigurationParameters: MutableList<Triple<ServerLocation, Protocol, String>> = mutableListOf()

        serverConfigurations.forEach { server ->
            listOfConfigurationParameters.add(Triple(server.serverLocation, Protocol.WIREGUARD, server.wireGuard.configfileurl))
        }

        return channelFlow {
            flowOf(listOfConfigurationParameters).collectLatest { ids ->
                combine(
                        ids.map { id -> configureWireGuardServer(id) }
                ) {
                    it.toList()
                }.collect {
                    send(it)
                }
            }
        }
    }


    private fun configureWireGuardServer(configDetails: Triple<ServerLocation, Protocol, String>): Flow<ServerCacheModel> {
        return fileDownloader.downloadWireGuardConfig(configDetails.first, configDetails.third)
                .map {
                    configureWireGuardTunnelForServer(it.getOrNull() as ServerSetUp.WireGuardSetup)
                }.map { server ->
                    Log.d(WireGuardConfigurator::class.java.simpleName, "Saving WireGuard: $server")
                    val serverCacheModel = saveWireGuardProfile(server as VPNServer.WireGuardServer)
                    serverCacheModel!!
                }
    }

    private fun configureWireGuardTunnelForServer(serverSetUp: ServerSetUp.WireGuardSetup): VPNServer {
        return configImporter.importConfigProfile("file://${serverSetUp.confFileDir}".toUri()).let {
            File(serverSetUp.confFileDir).delete()
            VPNServer.WireGuardServer(it, serverSetUp.serverLocation)
        }
    }

    private suspend fun saveWireGuardProfile(result: VPNServer.WireGuardServer): ServerCacheModel? {
        var serverCacheModel: ServerCacheModel? = null
        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        location?.let {
            serverCacheModel = result.toServerCacheModel(location)
            serverCacheModel?.let {
                serversDao.insert(it)
            } ?: throw IllegalStateException("Error saving null Wireguard tunnel profile $result")
            Log.d(WireGuardConfigurator::class.java.simpleName, "Saved: $result")
        } ?: throw IllegalStateException("Error saving null Wireguard location $location")
        return serverCacheModel
    }
}