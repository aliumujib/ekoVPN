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
import com.ekovpn.android.data.repositories.config.*
import com.ekovpn.android.data.repositories.config.VPNServer.IkeV2Server.Companion.toServerCacheModel
import com.ekovpn.android.data.repositories.config.downloader.FileDownloader
import com.ekovpn.android.data.repositories.config.importer.Ikev2CertificateImporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.strongswan.android.data.VpnProfileDataSource
import java.lang.IllegalStateException
import javax.inject.Inject

@ExperimentalCoroutinesApi
class IkeV2Configurator @Inject constructor(
        private val fileDownloader: FileDownloader,
        private val serversDao: ServersDao,
        private val locationsDao: LocationsDao,
        private val vpnProfileDataSource: VpnProfileDataSource,
        private val iKev2CertificateImporter: Ikev2CertificateImporter
) {


    fun configureIkeV2Servers(serverConfig: List<ServerConfig>): Flow<List<ServerCacheModel>> {
        vpnProfileDataSource.deleteAllVpnProfiles()

        val pemConfigs: MutableList<Pair<ServerLocation, IKEv2>> = mutableListOf()

        serverConfig.forEach { server ->
            pemConfigs.add(Pair(server.serverLocation, server.iKev2))
        }

//        return flowOf(pemConfigs).flatMapLatest { ids -> runOnAll(ids.toSet(), ::configureIkeV2Server as (Pair<ServerLocation, IKEv2>) -> Flow<ServerCacheModel>) }
//                .map { m -> m.values.toList() }

        return channelFlow{
            flowOf(pemConfigs).collectLatest { ids ->
                combine(
                        ids.map { id -> configureIkeV2Server(id) }
                ) {
                    it.toList()
                }.collect {
                    send(it)
                }
            }
        }
    }


    private fun configureIkeV2Server(configurationDetails: Pair<ServerLocation, IKEv2>): Flow<ServerCacheModel> {
        Log.d(IkeV2Configurator::class.java.simpleName, "IkeV2 $configurationDetails")

        return fileDownloader.downloadIKev2Certificate(configurationDetails.first, configurationDetails.second)
                .map {
                    Log.d(IkeV2Configurator::class.java.simpleName, "Configuring ikeV2: ${it.getOrNull()}")
                    val setUp = it.getOrNull() as ServerSetUp.IkeV2Setup
                    iKev2CertificateImporter.importServerConfig(setUp.pemFileDir.toUri(), setUp.serverLocation, setUp.ikeV2)
                }.map { server ->
                    Log.d(IkeV2Configurator::class.java.simpleName, "Saving ikeV2: $server")
                    val serverCacheModel = saveIkeV2Profile(server as VPNServer.IkeV2Server)
                    serverCacheModel!!
                }
    }


    private suspend fun saveIkeV2Profile(result: VPNServer.IkeV2Server): ServerCacheModel? {
        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        var serverCacheModel: ServerCacheModel? = null
        location?.let {
            serverCacheModel = result.toServerCacheModel(location)
            val savedProfile = vpnProfileDataSource.insertProfile(result.ikeV2Profile)
            serverCacheModel = serverCacheModel?.copy(ikeV2ProfileId = savedProfile.id)
            serverCacheModel?.let {
                serversDao.insert(it)
            } ?: throw IllegalStateException("Error saving null IKEv2 server $serverCacheModel")
            Log.d(IkeV2Configurator::class.java.simpleName, "Saved: $result")
        } ?: throw IllegalStateException("Error saving null IKEv2 location $location")

        return serverCacheModel
    }

}