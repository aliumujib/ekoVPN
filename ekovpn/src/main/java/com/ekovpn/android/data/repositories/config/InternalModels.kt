/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.config

import com.ekovpn.android.data.cache.room.entities.LocationCacheModel
import com.ekovpn.android.data.cache.room.entities.ServerCacheModel
import com.ekovpn.android.models.Protocol
import com.google.gson.annotations.SerializedName


data class ServerConfig(
        @SerializedName("location")
        val serverLocation: ServerLocation,
        val open_vpn: List<OpenVpn>,
        @SerializedName("ikev2")
        val iKev2: IKEv2
)

data class IKEv2(
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
        fun ServerLocation.toLocationCacheModel(index: Int): LocationCacheModel {
            return LocationCacheModel(city = this.city, country = this.country, country_code = this.country_code, locationId = index)
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
                          val ikeV2: IKEv2, val protocol: Protocol) : ServerSetUp(serverLocation, protocol)
}


sealed class VPNServer(
        val serverLocation_: ServerLocation,
        val protocol_: Protocol
) {
    data class OVPNServer(val openVpnProfile: de.blinkt.openvpn.VpnProfile, val serverLocation: ServerLocation, val protocol: Protocol) : VPNServer(serverLocation, protocol) {
        companion object {
            fun OVPNServer.toServerCacheModel(locationCacheModel: LocationCacheModel, protocol: Protocol): ServerCacheModel {
                return ServerCacheModel(0, location = locationCacheModel.locationId, protocol = protocol.value, ovpnProfileId = openVpnProfile.uuidString, ikeV2ProfileId = null)
            }
        }
    }

    data class IkeV2Server(val ikeV2Profile: org.strongswan.android.data.VpnProfile, val serverLocation: ServerLocation) : VPNServer(serverLocation, Protocol.IKEv2) {
        companion object {
            fun IkeV2Server.toServerCacheModel(locationCacheModel: LocationCacheModel): ServerCacheModel {
                return ServerCacheModel(0, location = locationCacheModel.locationId, protocol = Protocol.IKEv2.value, ovpnProfileId = null, ikeV2ProfileId = ikeV2Profile.id)
            }
        }
    }

}



