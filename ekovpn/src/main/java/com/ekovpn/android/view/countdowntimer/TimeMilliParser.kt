/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.countdowntimer

class TimeMilliParser {

    fun parseTimeInMilliSeconds(timeLeft: Long): String {
        val days = timeLeft / (3600 * 24 *  1000)
        val hours = timeLeft / (3600 * 1000)
        val minutes = timeLeft / (60 * 1000)
        val seconds = (timeLeft/1000) % (60)

        return "${"%02d".format(hours)} : ${"%02d".format(minutes)} :${"%02d".format(seconds)}"
    }

}