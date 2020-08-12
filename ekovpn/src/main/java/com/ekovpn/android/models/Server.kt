/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.models

import android.os.Parcelable
import com.ekovpn.android.data.cache.room.entities.ServerLocationModel
import kotlinx.android.parcel.Parcelize


@Parcelize
open class Server(
        val id_: Int,
        val location_: Location,
        val protocol_: Protocol) : Parcelable {

    @Parcelize
    data class OVPNServer(val ovpnProfileId: String,
                          val id: Int,
                          val location: Location,
                          val protocol: Protocol) : Server(id, location, protocol), Parcelable {
        companion object {
            fun fromServerCacheModel(serverLocationModel: ServerLocationModel): OVPNServer {
                return OVPNServer(serverLocationModel.serverCacheModel.ovpnProfileId!!,
                        serverLocationModel.serverCacheModel.serverId,
                        Location.fromLocationCacheModel(serverLocationModel.locationCacheModel),
                        Protocol.fromString(serverLocationModel.serverCacheModel.protocol))
            }
        }
    }

    @Parcelize
    data class IkeV2Server(val profileId: Long,
                           val id: Int,
                           val location: Location) : Server(id, location, Protocol.IKEv2), Parcelable {
        companion object {
            fun fromServerCacheModel(serverLocationModel: ServerLocationModel): IkeV2Server {
                return IkeV2Server(serverLocationModel.serverCacheModel.ikeV2ProfileId!!,
                        serverLocationModel.serverCacheModel.serverId,
                        Location.fromLocationCacheModel(serverLocationModel.locationCacheModel))
            }
        }
    }

    @Parcelize
    data class WireGuardServer(val tunnelName: String,
                           val id: Int,
                           val location: Location) : Server(id, location, Protocol.IKEv2), Parcelable {
        companion object {
            fun fromServerCacheModel(serverLocationModel: ServerLocationModel): WireGuardServer {
                return WireGuardServer(serverLocationModel.serverCacheModel.tunnelName!!,
                        serverLocationModel.serverCacheModel.serverId,
                        Location.fromLocationCacheModel(serverLocationModel.locationCacheModel))
            }
        }
    }
}