/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.compoundviews.countdowntimer

import android.content.Context
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StopWatchTextView : AppCompatTextView {

    private val timeMilliParser = TimeMilliParser()
    var stopWatchHandler: Handler? = null
    var runnable : Runnable? = null
    var currentTime = 0L
    var interval = 1000L

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        parseTimeLeft(currentTime)
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)

    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    fun startStopWatch(milliseconds: Long) {
        stopWatchHandler = Handler()

        runnable = object : Runnable {
            override fun run() {
                if(currentTime < milliseconds){
                    val hours = (currentTime / 1000) / (60 * 60)
                    val minutes = (currentTime / 1000) / 60
                    val seconds = (currentTime / 1000) % 60

                    text = "${"%02d".format(hours)} : ${"%02d".format(minutes)} : ${"%02d".format(seconds)}"

                    Thread.sleep(1000)
                    currentTime += interval

                    stopWatchHandler?.post(this)
                }else{
                    //call some cute listener
                }
            }
        }

        stopWatchHandler?.post(runnable)

    }

    private fun parseTimeLeft(millisUntilFinished: Long) {
        text = timeMilliParser.parseTimeInMilliSeconds(millisUntilFinished)
    }

    fun resumeCountDownTimer() {
        startStopWatch(currentTime)
    }

    fun pauseCountDownTimer() {
        stopWatchHandler?.removeCallbacks(runnable)
    }

    fun resetTimer() {
        stopWatchHandler?.removeCallbacks(runnable)
        currentTime = 0L
        interval = 1L
        parseTimeLeft(currentTime)
    }

}