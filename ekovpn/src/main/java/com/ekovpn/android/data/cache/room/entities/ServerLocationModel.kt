/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.cache.room.entities

import androidx.room.Embedded


data class ServerLocationModel(
        @Embedded
        val serverCacheModel: ServerCacheModel,
        @Embedded
        val locationCacheModel: LocationCacheModel
)

