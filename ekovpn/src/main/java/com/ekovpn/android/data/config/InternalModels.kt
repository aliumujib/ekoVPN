/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config

import com.ekovpn.android.cache.room.entities.LocationCacheModel
import com.ekovpn.android.cache.room.entities.ServerCacheModel
import com.ekovpn.android.models.Protocol
import com.google.gson.annotations.SerializedName
import de.blinkt.openvpn.VpnProfile


data class ServerConfig(
        @SerializedName("location")
        val serverLocation: ServerLocation,
        val open_vpn: List<OpenVpn>
)

data class ServerLocation(
        val city: String,
        val country: String,
        val country_code: String
) {
    companion object {
        fun toLocationCacheModel(it: ServerLocation): LocationCacheModel {
            return LocationCacheModel(city = it.city, country = it.country, country_code = it.country_code, locationId = 0)
        }
    }
}

data class OpenVpn(
        val configfileurl: String,
        val protocol: String
)


sealed class ServerSetUp(val serverLocation_: ServerLocation,
                         val protocol_: Protocol) {

    data class OVPNSetup(val ovpnFileDir: String, val ovpnProfileId: String = "N/A", val serverLocation: ServerLocation,
                         val protocol: Protocol) : ServerSetUp(serverLocation, protocol)
}


sealed class VPNServer(
        val serverLocation_: ServerLocation,
        val protocol_: Protocol
) {
    data class OVPNServer(val openVpnProfile: VpnProfile, val serverLocation: ServerLocation, val protocol: Protocol) : VPNServer(serverLocation, protocol) {
        companion object {
            fun toServerCacheModel(locationCacheModel: LocationCacheModel,
                                   protocol: Protocol, profileUUID: String): ServerCacheModel {
                return ServerCacheModel(0, location = locationCacheModel.locationId, protocol = protocol.value, ovpnProfileId = profileUUID)
            }
        }
    }

}


