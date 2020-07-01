/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.model

sealed class ServerSetUp(val location_: Location,
                         val protocol_: Protocol) {

    data class OVPNSetup(val ovpnFileDir: String, val ovpnProfileId: String = "N/A", val location: Location,
                         val protocol: Protocol) : ServerSetUp(location, protocol)
}

