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
        return getPref(TIME_LEFT, 600000L)
    }

    companion object {
        const val TIME_LEFT = "TIME_LEFT"
    }

}