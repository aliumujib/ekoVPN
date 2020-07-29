/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.user

import com.ekovpn.android.data.cache.settings.UserPrefManager
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val userPrefManager: UserPrefManager) : UserRepository {

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

}