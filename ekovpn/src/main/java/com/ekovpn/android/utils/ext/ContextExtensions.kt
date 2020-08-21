/*
 * Copyright 2020 Abdul-Mujeeb Aliu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ekovpn.android.utils.ext

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.TELEPHONY_SERVICE
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.math.roundToInt


/**
 * Get resource string from optional id
 *
 * @param resId Resource string identifier.
 * @return The key value if exist, otherwise empty.
 */
fun Context.getString(@StringRes resId: Int?) =
        resId?.let {
            getString(it)
        } ?: run {
            ""
        }


fun Context.dpToPx(dp: Int): Int {
    var displayMetrics = resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun Context.getColorHexString(@ColorRes resId: Int): String {
    val colorInt = ContextCompat.getColor(this, resId)
    return String.format("#%06X", 0xFFFFFF and colorInt)
}


fun Context.getIpAddress(): String? {
    val wifiManager: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    var ipAddress: String = intToInetAddress(wifiManager.dhcpInfo.ipAddress).toString()
    ipAddress = ipAddress.substring(1)
    return ipAddress
}

fun intToInetAddress(hostAddress: Int): InetAddress {
    val addressBytes = byteArrayOf((0xff and hostAddress).toByte(),
            (0xff and (hostAddress shr 8)).toByte(),
            (0xff and (hostAddress shr 16)).toByte(),
            (0xff and (hostAddress shr 24)).toByte())
    return try {
        InetAddress.getByAddress(addressBytes)
    } catch (e: UnknownHostException) {
        throw AssertionError()
    }
}

fun Context.copyToClipBoard(text: String) {
    val clipboard: ClipboardManager? = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
    val clip: ClipData = ClipData.newPlainText("eko_vpn_acc", text)
    clipboard?.setPrimaryClip(clip)
}

fun Context.showAlertDialog(positiveAction: () -> Unit, negativeAction: () -> Unit, title: String) {
    val builder1: AlertDialog.Builder = AlertDialog.Builder(this)
    builder1.setMessage(title)
    builder1.setCancelable(false)

    builder1.setPositiveButton(
            "Yes"
    ) { dialog, id ->
        positiveAction.invoke()
        dialog.cancel()
    }

    builder1.setNegativeButton(
            "No"
    ) { dialog, id ->
        negativeAction.invoke()
        dialog.cancel()
    }

    val alert11: AlertDialog = builder1.create()

    alert11.setOnShowListener {
        val negativeBtn: Button = alert11.getButton(DatePickerDialog.BUTTON_NEGATIVE)
        negativeBtn.setColors()

        val positiveBtn: Button = alert11.getButton(DatePickerDialog.BUTTON_POSITIVE)
        positiveBtn.setColors()
    }



    alert11.show()
}


@SuppressLint("HardwareIds")
fun Context.getDeviceId(): String? {
    val mTelephony: TelephonyManager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
    var deviceId: String? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        deviceId =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mTelephony.phoneCount == 2) {
                mTelephony.getImei(0)
            } else {
                mTelephony.imei
            }
        } else {
            if (mTelephony.phoneCount == 2) {
                mTelephony.getDeviceId(0)
            } else {
                mTelephony.deviceId
            }
        }
    } else {
        deviceId = mTelephony.deviceId
    }
    return deviceId
}