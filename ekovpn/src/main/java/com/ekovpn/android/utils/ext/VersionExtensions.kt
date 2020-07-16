/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.utils.ext

import android.os.Build
import androidx.core.os.BuildCompat

/**
 * @return `true` if the device is [Build.VERSION_CODES.M] or later
 */
fun isMOrLater(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

/**
 * @return `true` if the device is [Build.VERSION_CODES.N] or later
 */
fun isNOrLater(): Boolean {
    return BuildCompat.isAtLeastN()
}
