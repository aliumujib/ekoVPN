/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.auth

import com.ekovpn.android.models.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun createAccount(): Flow<User>

    fun loginToApp(): Flow<String>

    suspend fun isUserLoggedIn(): Boolean

    fun fetchUserByAccountNumber(accountNumber: String): Flow<User>
}