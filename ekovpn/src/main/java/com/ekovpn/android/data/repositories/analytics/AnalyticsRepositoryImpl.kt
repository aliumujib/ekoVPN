/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.analytics

import android.os.Bundle
import com.ekovpn.android.models.Protocol
import com.ekovpn.android.models.Server
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
        private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsRepository {

    override fun logServerConnectionEvent(server: Server) {
        val bundle = Bundle()
        bundle.putString("server_location", server.location_.name)
        logProtocolConnectionEvent(server.protocol_)
        firebaseAnalytics.logEvent(AnalyticsConstants.LOCATION_CONNECTION, bundle)
    }

    override fun logProtocolConnectionEvent(protocol: Protocol) {
        val bundle = Bundle()
        bundle.putString("server_protocl", protocol.value)
        firebaseAnalytics.logEvent(AnalyticsConstants.LOCATION_CONNECTION, bundle)
    }

}