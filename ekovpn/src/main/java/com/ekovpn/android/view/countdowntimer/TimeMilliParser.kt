/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.countdowntimer

class TimeMilliParser {

    fun parseTimeInMilliSeconds(timeLeft: Long): String {

        val hours = ((timeLeft % DAY) / HOUR)
        val minutes = ((timeLeft % HOUR) / MINUTE)
        val seconds = ((timeLeft % MINUTE) / SECOND)

        return "${"%02d".format(hours)} : ${"%02d".format(minutes)} :${"%02d".format(seconds)}"
    }

    companion object {
        const val SECOND = 1000 // no. of ms in a second

        const val MINUTE = SECOND * 60 // no. of ms in a minute

        const val HOUR = MINUTE * 60 // no. of ms in an hour

        const val DAY = HOUR * 24 // no. of ms in a day

        const val WEEK = DAY * 7
    }

}