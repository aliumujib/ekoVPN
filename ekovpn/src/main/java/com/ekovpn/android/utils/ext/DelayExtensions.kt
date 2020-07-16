/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.utils.ext

import android.os.Handler

fun delay(function: () -> Unit, timeMillis: Long = 1000) {
    val handler = Handler()
    handler.postDelayed({
        function.invoke()
    }, timeMillis)
}


