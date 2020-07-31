/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.user

import com.ekovpn.android.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getTimeLeft(): Long

    fun setTimeLeft(timeLeft: Long)

    fun addToTimeLeft(newTime: Long)

    fun getCurrentUser(): Flow<User>

}