/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.user

import com.ekovpn.android.data.cache.room.dao.UsersDao
import com.ekovpn.android.data.cache.settings.UserPrefManager
import com.ekovpn.android.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val userPrefManager: UserPrefManager, private val usersDao: UsersDao) : UserRepository {

    override fun getTimeLeft(): Long {
        return userPrefManager.getTimeLeft()
    }

    override fun setTimeLeft(timeLeft: Long) {
        userPrefManager.setTimeLeft(timeLeft)
    }

    override fun addToTimeLeft(newTime: Long) {
        val original = getTimeLeft()
        val new = original + newTime
        setTimeLeft(new)
    }

    override fun getCurrentUser(): Flow<User> {
        return usersDao.streamUser().map {
            it.toUser()
        }.flowOn(Dispatchers.IO)
    }

}