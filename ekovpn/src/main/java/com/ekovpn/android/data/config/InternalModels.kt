/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config

import com.ekovpn.android.cache.room.entities.LocationCacheModel
import com.ekovpn.android.cache.room.entities.ServerCacheModel
import com.ekovpn.android.models.Protocol
import com.google.gson.annotations.SerializedName


data class ServerConfig(
        @SerializedName("location")
        val serverLocation: ServerLocation,
        val open_vpn: List<OpenVpn>,
        val ikev2: IkeV2
)

data class IkeV2(
        val certificate_url: String,
        val ip: String,
        val password: String,
        val username: String
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

    data class IkeV2Setup(val pemFileDir: String, val pemProfileID: String = "N/A", val serverLocation: ServerLocation,
                          val ikeV2: IkeV2, val protocol: Protocol) : ServerSetUp(serverLocation, protocol)
}


sealed class VPNServer(
        val serverLocation_: ServerLocation,
        val protocol_: Protocol
) {
    data class OVPNServer(val openVpnProfile: de.blinkt.openvpn.VpnProfile, val serverLocation: ServerLocation, val protocol: Protocol) : VPNServer(serverLocation, protocol) {
        companion object {
            fun OVPNServer.toServerCacheModel(locationCacheModel: LocationCacheModel, protocol: Protocol): ServerCacheModel {
                return ServerCacheModel(0, location = locationCacheModel.locationId, protocol = protocol.value, ovpnProfileId = openVpnProfile.uuidString, ikeV2Alias = null)
            }
        }
    }

    data class IkeV2Server(val ikeV2Profile: org.strongswan.android.data.VpnProfile, val serverLocation: ServerLocation) : VPNServer(serverLocation, Protocol.IKEV2) {
        companion object {
            fun IkeV2Server.toServerCacheModel(locationCacheModel: LocationCacheModel): ServerCacheModel {
                return ServerCacheModel(0, location = locationCacheModel.locationId, protocol = Protocol.IKEV2.value, ovpnProfileId = null, ikeV2Alias = ikeV2Profile.certificateAlias)
            }
        }
    }

}



