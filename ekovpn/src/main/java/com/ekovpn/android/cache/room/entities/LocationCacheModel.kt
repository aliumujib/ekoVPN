/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.cache.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LocationCacheModel(
        @PrimaryKey
        val locationId: Int,
        val city: String,
        val country: String,
        val country_code: String
)
