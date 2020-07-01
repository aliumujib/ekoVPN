/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.servers

import com.ekovpn.android.cache.room.dao.ServersDao
import com.ekovpn.android.data.settings.SettingsRepository
import com.ekovpn.android.models.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ServersRepositoryImpl @Inject constructor(val serversDao: ServersDao,
                                                val settingsRepository: SettingsRepository) : ServersRepository {

    override fun getServersForCurrentProtocol(): Flow<List<Server>> {
        return serversDao.getServersForProtocol(settingsRepository.getSelectedProtocol().value).map {
            it.map {
                Server.OVPNServer.fromServerCacheModel(it)
            }
        }
    }

}