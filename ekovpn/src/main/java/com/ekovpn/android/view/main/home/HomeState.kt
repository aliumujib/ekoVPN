/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import com.ekovpn.android.models.Location
import com.ekovpn.android.models.Server
import com.ekovpn.android.models.User

data class HomeState(
        val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
        val _serversList: List<Server> = emptyList(),
        val lastUsedServer: Server? = null,
        val currentConnectionServer: Server? = null,
        val currentLocation: Location? = null,
        val _error: Throwable? = null,
        val timeLeft: Long  = 0L,
        val user: User?  = null,
        val hasShownBalloonCTA:Boolean = false
) {
    enum class ConnectionStatus(val status: Int) {
        CONNECTED(1),
        DISCONNECTED(2),
        CONNECTING(3),
    }
}