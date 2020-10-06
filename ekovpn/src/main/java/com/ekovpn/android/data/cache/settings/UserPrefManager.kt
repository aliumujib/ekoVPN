/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.cache.settings

import android.content.Context
import com.ekovpn.android.data.cache.sharedprefs.CoreSharedPrefManager
import javax.inject.Inject

class UserPrefManager @Inject constructor(val context: Context) : CoreSharedPrefManager(context) {

    fun setTimeLeft(timeLeft: Long) {
        savePref(TIME_LEFT, timeLeft)
    }

    fun getTimeLeft(): Long {
        return getPref(TIME_LEFT, 3600001L)
    }

    fun setUserAccountId(id: String) {
        savePref(USER_ID, id)
    }

    fun setHasScheduledReseter(hasScheduledReseter: Boolean) {
        savePref(FIRST_RUN, hasScheduledReseter)
    }

    fun hasScheduledReseter(): Boolean {
        return getPref(FIRST_RUN, false)
    }

    fun getUserId(): String? {
        return getPref(USER_ID, null)
    }


    companion object {
        const val USER_ID = "USER_ID"
        const val TIME_LEFT = "TIME_LEFT"
        const val FIRST_RUN = "FIRST_RUN"
    }

}