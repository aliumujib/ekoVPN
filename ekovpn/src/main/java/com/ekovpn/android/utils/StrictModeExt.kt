/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.utils

import android.os.Build
import android.os.StrictMode
import android.os.strictmode.Violation
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.P)
fun StrictMode.VmPolicy.Builder.detectAllExpect(ignoredViolationPackageName: String, justVerbose: Boolean = true): StrictMode.VmPolicy.Builder {
    return detectAll()
            .penaltyListener(Executors.newSingleThreadExecutor(), StrictMode.OnVmViolationListener
            {
                it.filter(ignoredViolationPackageName, justVerbose)
            })
}

@RequiresApi(Build.VERSION_CODES.P)
private fun Violation.filter(ignoredViolationPackageName: String, justVerbose: Boolean) {
    val violationPackageName = stackTrace[0].className
    if (violationPackageName != ignoredViolationPackageName && justVerbose) {
        Log.d(violationPackageName, this.toString())
    }

}