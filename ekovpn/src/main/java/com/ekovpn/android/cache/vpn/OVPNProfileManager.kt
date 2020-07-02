/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.cache.vpn

import android.content.Context
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ProfileManager
import javax.inject.Inject

class OVPNProfileManager @Inject constructor(private val context: Context, private val profileManager: ProfileManager) {


    fun saveProfile(profile: VpnProfile) {
        profileManager.saveProfile(context, profile)
    }



}