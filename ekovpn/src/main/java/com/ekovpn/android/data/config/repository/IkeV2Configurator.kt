/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.repository

import android.util.Log
import androidx.core.net.toUri
import com.ekovpn.android.cache.room.dao.LocationsDao
import com.ekovpn.android.cache.room.dao.ServersDao
import com.ekovpn.android.data.config.ServerConfig
import com.ekovpn.android.data.config.ServerSetUp
import com.ekovpn.android.data.config.VPNServer
import com.ekovpn.android.data.config.VPNServer.IkeV2Server.Companion.toServerCacheModel
import com.ekovpn.android.data.config.downloader.FileDownloader
import com.ekovpn.android.data.config.importer.Ikev2CertificateImporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.strongswan.android.data.VpnProfileDataSource
import javax.inject.Inject

@ExperimentalCoroutinesApi
class IkeV2Configurator @Inject constructor(
        private val fileDownloader: FileDownloader,
        private val serversDao: ServersDao,
        private val locationsDao: LocationsDao,
        private val vpnProfileDataSource: VpnProfileDataSource,
        private val iKev2CertificateImporter: Ikev2CertificateImporter
) {

    fun configureIkeV2Servers(serverConfig: Array<ServerConfig>): Flow<Result<Unit>> {
        vpnProfileDataSource.deleteAllVpnProfiles()
        serverConfig.forEach {
            Log.d(IkeV2Configurator::class.java.simpleName, "IkeV2 ${it.serverLocation}")
        }

        val pemConfigs: MutableList<Flow<Result<ServerSetUp>>> = mutableListOf()

        serverConfig.forEach { server ->
            pemConfigs.add(fileDownloader.downloadIKev2Certificate(server.serverLocation, server.iKev2))
        }

        return pemConfigs.merge()
                .flatMapConcat {
                    if (it.isSuccess) {
                        Log.d(IkeV2Configurator::class.java.simpleName, "Configuring ikeV2: ${it.getOrNull()}")
                        val setUp = it.getOrNull() as ServerSetUp.IkeV2Setup
                        iKev2CertificateImporter.importServerConfig(setUp.pemFileDir.toUri(), setUp.serverLocation, setUp.ikeV2)
                    } else {
                        Log.d(IkeV2Configurator::class.java.simpleName, "Failed to configure ikeV2: ${it.getOrNull()}")
                        flowOf(Result.failure(Exception("An error occurred for config ${it.getOrNull()}")))
                    }
                }.map { profile ->
                    if (profile.getOrNull() is VPNServer) {
                        Log.d(IkeV2Configurator::class.java.simpleName, "Saving ikeV2: ${profile.getOrNull()}")
                        saveIkeV2Profile(profile.getOrNull() as VPNServer.IkeV2Server)
                        Result.success(Unit)
                    } else {
                        Log.d(IkeV2Configurator::class.java.simpleName, "Failed Saving ikeV2: ${profile.getOrNull()}")
                        Result.failure(Exception("An error occurred for config"))
                    }
                }
    }


    private suspend fun saveIkeV2Profile(result: VPNServer.IkeV2Server) {
        val location = locationsDao.getLocation(result.serverLocation.country, result.serverLocation.city)
        location?.let {
            val serverCacheModel = result.toServerCacheModel(location)
            vpnProfileDataSource.insertProfile(result.ikeV2Profile)
            serversDao.insert(serverCacheModel)
            Log.d(IkeV2Configurator::class.java.simpleName, "Saved: $result")
        }
    }

}