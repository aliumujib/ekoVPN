/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.servers

import com.ekovpn.android.models.Location
import com.ekovpn.android.models.Server
import kotlinx.coroutines.flow.Flow

interface ServersRepository {

    fun getServersForCurrentProtocol(): Flow<List<Server>>

    fun saveLastUsedServer(location: Int)

    fun getLastUsedLocation(): Flow<Server>

    fun getCurrentLocation(): Flow<Location>

    suspend fun getOVPNProfileForServer(profileUUID: String): de.blinkt.openvpn.VpnProfile?

    suspend fun getIkev2ProfileForServer(alias: String): org.strongswan.android.data.VpnProfile?

}