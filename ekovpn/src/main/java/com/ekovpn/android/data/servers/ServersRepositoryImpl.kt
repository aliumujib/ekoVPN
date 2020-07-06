/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.servers

import android.util.Log
import com.ekovpn.android.BuildConfig
import com.ekovpn.android.cache.room.dao.ServersDao
import com.ekovpn.android.data.settings.SettingsRepository
import com.ekovpn.android.models.Location
import com.ekovpn.android.models.Server
import com.ekovpn.android.remote.retrofit.AWSIPApiService
import com.ekovpn.android.remote.retrofit.IPStackApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ServersRepositoryImpl @Inject constructor(private val serversDao: ServersDao,
                                                private val ipStackApiService: IPStackApiService,
                                                private val awsipApiService: AWSIPApiService,
                                                private val settingsRepository: SettingsRepository) : ServersRepository {

    override fun getServersForCurrentProtocol(): Flow<List<Server>> {
        return serversDao.getServersForProtocol(settingsRepository.getSelectedProtocol().value).map {
            it.map { server ->
                Server.OVPNServer.fromServerCacheModel(server)
            }
        }
    }

    override fun saveLastUsedServer(serverId: Int) {
        settingsRepository.saveLastServerId(serverId)
    }

    override fun getLastUsedLocation(): Flow<Server> {
        return serversDao.getServersForProtocol(settingsRepository.getSelectedProtocol().value).map {
            it.filter {
                it.serverCacheModel.serverId == settingsRepository.getLastServerId()
            }.map {
                Server.OVPNServer.fromServerCacheModel(it)
            }.firstOrNull()
        }.filter {
            it != null
        }.map {
            it!!
        }
    }

    override fun getCurrentLocation(): Flow<Location> {
        return flow {
            delay(1000)
            val ipAddress = awsipApiService.fetchIP().body()?.string() ?: "0.0.0.0"
            Log.d("TAG", ipAddress)
            emit(ipStackApiService.resolveIpToLocation(ipAddress, BuildConfig.IP_STACK_API_KEY))
        }.map {
            Location(-1, it.city, it.country_name, it.country_code)
        }
    }

}