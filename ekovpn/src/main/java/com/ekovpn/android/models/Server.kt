/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.models

import com.ekovpn.android.cache.room.entities.ServerLocationModel

sealed class Server(
        val id_: Int,
        val location_: Location,
        val protocol_: Protocol) {

    data class OVPNServer(val ovpnProfileId: String,
                          val id: Int,
                          val location: Location,
                          val protocol: Protocol) : Server(id, location, protocol) {
        companion object {
            fun fromServerCacheModel(serverLocationModel: ServerLocationModel): OVPNServer {
                return OVPNServer(serverLocationModel.serverCacheModel.ovpnProfileId!!,
                        serverLocationModel.serverCacheModel.serverId,
                        Location.fromLocationCacheModel(serverLocationModel.locationCacheModel),
                        Protocol.fromString(serverLocationModel.serverCacheModel.protocol))
            }
        }
    }

    data class IkeV2Server(val certificateAlias: String,
                           val id: Int,
                           val location: Location) : Server(id, location, Protocol.IKEV2) {
        companion object {
            fun fromServerCacheModel(serverLocationModel: ServerLocationModel): IkeV2Server {
                return IkeV2Server(serverLocationModel.serverCacheModel.ikeV2Alias!!,
                        serverLocationModel.serverCacheModel.serverId,
                        Location.fromLocationCacheModel(serverLocationModel.locationCacheModel))
            }
        }
    }
}