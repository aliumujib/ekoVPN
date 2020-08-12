/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.cache.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [(ForeignKey(entity = LocationCacheModel::class,
        parentColumns = ["locationId"], childColumns = ["location"],
        onDelete = ForeignKey.CASCADE))])
data class ServerCacheModel(
        @PrimaryKey(autoGenerate = true)
        val serverId: Int,
        val location: Int,
        val protocol: String,
        val ovpnProfileId: String?,
        val ikeV2ProfileId: Long?,
        val tunnelName:String?
)

