/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.countdowntimer

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CountDownTimerTextView : AppCompatTextView {

    private val timeMilliParser = TimeMilliParser()
    var countDownTimer : CountDownTimer? = null
    var timeLeft = 300000L
    var interval = 1L

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        parseTimeLeft(timeLeft)
    }

     fun startCountDown(milliseconds:Long, interval:Long){
        countDownTimer = object : CountDownTimer(milliseconds, interval) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                parseTimeLeft(millisUntilFinished)
            }

        }
        countDownTimer?.start()
    }

    private fun parseTimeLeft(millisUntilFinished: Long) {
        text = timeMilliParser.parseTimeInMilliSeconds(millisUntilFinished)
    }

    fun resumeCountDownTimer(){
        startCountDown(timeLeft, interval)
    }

    fun pauseCountDownTimer(){
        countDownTimer?.cancel()
    }

    fun resetTimer(){
        countDownTimer?.cancel()
        timeLeft = 300000L
        interval = 1L
        parseTimeLeft(timeLeft)
    }

}