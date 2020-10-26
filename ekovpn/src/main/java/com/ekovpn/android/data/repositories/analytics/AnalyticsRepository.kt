/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.analytics

import com.ekovpn.android.models.Protocol
import com.ekovpn.android.models.Server
import com.ekovpn.android.models.User
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {

    fun logServerConnectionEvent(server: Server)
    fun logProtocolConnectionEvent(protocol: Protocol)

}