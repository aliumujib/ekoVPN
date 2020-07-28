/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.cache.settings

import android.content.Context
import com.ekovpn.android.data.cache.sharedprefs.CoreSharedPrefManager
import com.ekovpn.android.models.Protocol
import javax.inject.Inject

class SettingsPrefManager @Inject constructor(val context: Context) : CoreSharedPrefManager(context) {

    fun setHasCompletedSetup() {
        savePref(HAS_COMPLETED_SETUP, true)
    }

    fun getHasCompletedSetup(): Boolean {
        return getPref(HAS_COMPLETED_SETUP, false)
    }

    fun getLastSelectedProtocol(): Protocol {
        return Protocol.fromString(getPref(LAST_SELECTED_PROTOCOL, Protocol.TCP.value))
    }

    fun setLastSelectedProtocol(protocol: Protocol) {
        savePref(LAST_SELECTED_PROTOCOL, protocol.value)
    }

    fun saveLastServerId(id: Int) {
        return savePref(LAST_USED_SERVER, id)
    }

    fun getLastServerId(): Int {
        return getPref(LAST_USED_SERVER, -1)
    }


    companion object {
        const val HAS_COMPLETED_SETUP = "HAS_COMPLETED_SETUP"
        const val LAST_SELECTED_PROTOCOL = "LAST_SELECTED_PROTOCOL"
        const val LAST_USED_SERVER = "LAST_USED_SERVER"
    }

}