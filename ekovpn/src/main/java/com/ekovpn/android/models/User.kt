/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.models


data class User(
        val id: String,
        val account_id: String,
        val account_type: AccountType,
        val active: Boolean,
        val createdAt: String,
        val order_data: String,
        val order_number: String,
        val referral_id: String,
        val referred_by: String,
        val renewal_at: String,
        val role: String,
        val time_expiry: String,
        val updatedAt: String,
        val vpn_credits: Int,
        val devices: List<Device>
){

    enum class AccountType(val type: String) {
        PAID("paid"),
        FREE("free");

        companion object {
            @JvmStatic
            fun fromString(type: String): AccountType =
                    values().find { value -> value.type.toLowerCase() == type.toLowerCase() }
                            ?: FREE
        }
    }
}