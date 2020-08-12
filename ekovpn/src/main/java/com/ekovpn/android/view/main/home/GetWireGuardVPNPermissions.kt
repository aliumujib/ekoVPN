/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.wireguard.android.backend.GoBackend

class GetWireGuardVPNPermissions : ActivityResultContract<Unit, Boolean>() {

    override fun createIntent(context: Context, input: Unit?): Intent {
        return GoBackend.VpnService.prepare(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean? {
        if (resultCode != Activity.RESULT_OK) return false
        return true
    }

}