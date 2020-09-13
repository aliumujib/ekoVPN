/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.remote.models.auth

import com.ekovpn.android.data.cache.room.entities.DeviceCacheModel
import com.ekovpn.android.data.cache.room.entities.UserCacheModel
import com.ekovpn.android.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RemoteUser(
        val __v: Int,
        val _id: String,
        val account_number: String,
        val account_type: String,
        val active: Boolean,
        val createdAt: String,
        val order_data: String?,
        val order_number: String?,
        val referral_code: String,
        val referred_by: String?,
        val renewal_at: String?,
        val role: String,
        val time_expiry: String,
        val updatedAt: String,
        val vpn_credits: Int,
        val imeis: List<String>
) {


    private fun fromDeviceCacheModelString(value: String): RemoteDevice {
        val listType = object : TypeToken<RemoteDevice>() {
        }.type
        return Gson().fromJson(value, listType)
    }

    fun toUserCacheModel(): UserCacheModel {
        return UserCacheModel(_id, account_number, account_type, active, createdAt, order_data
                ?: NOT_AVAILABLE, order_number ?: NOT_AVAILABLE, referral_code, referred_by
                ?: NOT_AVAILABLE, renewal_at
                ?: NOT_AVAILABLE, role, time_expiry, updatedAt, vpn_credits, imeis.map {
            fromDeviceCacheModelString(it).toDeviceCacheModel()
        })
    }

    fun toUser(): User {
        return User(_id, account_number, User.AccountType.fromString(account_type), active, createdAt, order_data
                ?: NOT_AVAILABLE, order_number ?: NOT_AVAILABLE, referral_code, referred_by
                ?: NOT_AVAILABLE, renewal_at
                ?: NOT_AVAILABLE, role, time_expiry, updatedAt, vpn_credits, imeis.map {
            fromDeviceCacheModelString(it).toDevice()
        })
    }

    companion object {
        private const val NOT_AVAILABLE = "N/A"
    }
}