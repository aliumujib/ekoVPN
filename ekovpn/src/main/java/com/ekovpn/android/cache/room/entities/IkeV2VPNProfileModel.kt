/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.cache.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IkeV2VPNProfileModel(
        val name: String,
        val gateWay: String,
        val vpnType: Int,
        val password: String,
        val username: String,
        @PrimaryKey
        val certificateAlias: String
)