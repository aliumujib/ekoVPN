/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.servers

import com.ekovpn.android.cache.room.dao.ServersDao
import com.ekovpn.android.data.settings.SettingsRepository
import com.ekovpn.android.models.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ServersRepositoryImpl @Inject constructor(private val serversDao: ServersDao,
                                                private val settingsRepository: SettingsRepository) : ServersRepository {

    override fun getServersForCurrentProtocol(): Flow<List<Server>> {
        return serversDao.getServersForProtocol(settingsRepository.getSelectedProtocol().value).map {
            it.map {server->
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

}