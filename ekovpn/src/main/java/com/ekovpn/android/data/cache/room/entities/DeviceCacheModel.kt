/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.cache.room.entities
import com.ekovpn.android.models.Device

data class DeviceCacheModel(private val imei: String, private val device: String) {
    fun toDevice(): Device {
        return Device(imei, device)
    }
}