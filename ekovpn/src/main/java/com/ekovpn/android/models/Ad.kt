/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.models

import android.content.res.Resources
import android.os.Parcelable
import com.ekovpn.android.R
import com.ekovpn.android.data.ads.AdModel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.item_ad.view.*

@Parcelize
class Ad(val type: AdType, val count: Int, private val timeAddition: Long) : Parcelable {


    enum class AdType(val value: Int) {
        VIDEO(1),
        IMAGE(2);

        companion object {
            @JvmStatic
            fun fromInt(type: Int): AdType =
                    values().find { value -> value.value == type }
                            ?: VIDEO
        }
    }


    companion object {
        fun fromAdModel(adModel: AdModel): Ad {
            return Ad(AdType.fromInt(adModel.type), adModel.count, adModel.timeAddition)
        }

        fun Ad.getMinutes(resources: Resources): String {
            return if ((timeAddition / 3600000) > 1) {
                resources.getString(R.string.quantity_hours, (timeAddition / 3600000))
            } else {
                resources.getString(R.string.quantity_minutes, (timeAddition / 60000))
            }
        }

        fun Ad.getQuantityDescription(resources: Resources): String {
            return if (type == AdType.VIDEO) {
                resources.getQuantityString(R.plurals.plural_videos, count, count)
            } else
                resources.getQuantityString(R.plurals.plural_ads, count, count)
        }

        fun Ad.getIcon(): Int {
            return if ((count == 1) && (type == AdType.VIDEO)) {
                R.drawable.watch_one_video
            } else if ((count > 1) && (type == AdType.VIDEO)) {
                R.drawable.watch_more_videos
            } else {
                R.drawable.image_ad
            }
        }
    }


}

