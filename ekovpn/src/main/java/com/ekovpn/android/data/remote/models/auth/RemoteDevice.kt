/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.remote.models.auth

import com.ekovpn.android.data.cache.room.entities.DeviceCacheModel
import com.ekovpn.android.models.Device
import com.google.gson.Gson

data class RemoteDevice(private val imei: String, private val device: String) {

    fun toJSONString(): String {
        return Gson().toJson(this, this::class.java)
    }

    fun toDeviceCacheModel(): DeviceCacheModel {
        return DeviceCacheModel(imei, device)
    }

    fun toDevice(): Device {
        return Device(imei, device)
    }

}