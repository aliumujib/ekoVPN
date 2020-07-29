/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.models

import android.os.Parcelable
import com.ekovpn.android.data.cache.room.entities.LocationCacheModel
import kotlinx.android.parcel.Parcelize

@Parcelize
class Location(
        val id: Int,
        val city: String,
        val country: String,
        val country_code: String
): Parcelable{

    companion object{
        fun fromLocationCacheModel(cacheModel: LocationCacheModel): Location {
            return Location(cacheModel.locationId, cacheModel.city, cacheModel.country, cacheModel.country_code)
        }
    }
}