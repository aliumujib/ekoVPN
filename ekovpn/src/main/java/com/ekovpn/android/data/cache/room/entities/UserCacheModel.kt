/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.cache.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ekovpn.android.models.User

@Entity
data class UserCacheModel(
        @PrimaryKey
        val id: String,
        val account_id: String,
        val account_type: String,
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
        val vpn_credits: Int
) {

    fun toUser(): User {
        return User(id, account_id, User.AccountType.fromString(account_type), active, createdAt, order_data
                , order_number, referral_id, referred_by
                , renewal_at
                , role, time_expiry, updatedAt, vpn_credits)
    }
}