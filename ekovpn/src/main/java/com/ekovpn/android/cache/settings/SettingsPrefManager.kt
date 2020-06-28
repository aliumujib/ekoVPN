/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.cache.settings

import android.content.Context
import com.ekovpn.android.cache.core.CoreSharedPrefManager
import javax.inject.Inject

class SettingsPrefManager @Inject constructor(val context: Context) : CoreSharedPrefManager(context) {

    fun setHasCompletedSetup() {
        savePref(HAS_COMPLETED_SETUP, true)
    }

    fun getHasCompletedSetup(): Boolean {
        return getPref(HAS_COMPLETED_SETUP, false)
    }

    companion object {
        const val HAS_COMPLETED_SETUP = "HAS_COMPLETED_SETUP"
    }

}