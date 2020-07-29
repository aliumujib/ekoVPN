/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.text.format.DateUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.ekovpn.android.ApplicationClass
import com.ekovpn.android.ApplicationClass.Companion.EKO_NOTIFICATION_CHANNEL_ID
import com.ekovpn.android.R
import com.ekovpn.android.data.repositories.user.UserRepository
import com.ekovpn.android.di.service.DaggerCountDownTimerComponent
import com.ekovpn.android.models.Server
import com.ekovpn.android.utils.ext.isNOrLater
import com.ekovpn.android.view.compoundviews.countdowntimer.TimeMilliParser
import com.ekovpn.android.view.main.VpnActivity
import de.blinkt.openvpn.core.IOpenVPNServiceInternal
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.VpnStatus
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.logic.VpnStateService.LocalBinder
import javax.inject.Inject

// pub-7604868220609576.

class EkoVPNMgrService : Service() {

    private val timeMilliParser = TimeMilliParser()
    private var countDownTimer: CountDownTimer? = null
    private var timeLeft = 30000L
    private var interval = 1L
    private val listeners = mutableSetOf<TimeLeftListener>()
    var server: Server? = null
    private val binder = VPNTimerLocalBinder()

    @Inject
    lateinit var userRepository: UserRepository


    override fun onCreate() {
        super.onCreate()
        DaggerCountDownTimerComponent
                .builder()
                .coreComponent((application as ApplicationClass).coreComponent)
                .build()
                .inject(this)
    }

    fun registerListener(timeLeftListener: TimeLeftListener) {
        listeners.add(timeLeftListener)
    }

    fun unregisterListener(timeLeftListener: TimeLeftListener) {
        listeners.remove(timeLeftListener)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action == TIMER_SERVICE_INCREASE_TIME_LEFT_ACTION){
            increaseTimeLeft(intent)
        }else{
            timeVPNConnection(intent)
        }
        return START_NOT_STICKY
    }

    private fun increaseTimeLeft(intent: Intent) {
        intent.getLongExtra(TIMER_SERVICE_INCREMENT, 0L).let {
            if (it > 0L) {
                countDownTimer?.cancel()

                Log.d(EkoVPNMgrService::class.java.simpleName, "Server: ${server.toString()}")
                userRepository.addToTimeLeft(it)
                timeLeft = userRepository.getTimeLeft()
                runCountDownTimer()
                runNotification()
            }
        }
    }

    private fun timeVPNConnection(intent: Intent) {
        intent.getParcelableExtra<Server>(TIMER_SERVICE_VPN_PROFILE)?.let {
            disconnectCurrentVPN()
            countDownTimer?.cancel()

            server = intent.getParcelableExtra(TIMER_SERVICE_VPN_PROFILE)
            timeLeft = intent.getLongExtra(TIMER_SERVICE_TIME_LEFT, 0L)

            Log.d(EkoVPNMgrService::class.java.simpleName, "Server: ${server.toString()}")
            runCountDownTimer()
            runNotification()
        }
    }

    private fun runCountDownTimer() {
        countDownTimer = object : CountDownTimer(timeLeft, interval) {

            override fun onFinish() {
                disconnectCurrentVPN()
                server = null
                showViewAdsNotification()
                userRepository.setTimeLeft(0)
                stopForeground(true)
            }


            override fun onTick(millisUntilFinished: Long) {
                if ((millisUntilFinished % 1000L) == 0L) {
                    listeners.forEach {
                        userRepository.setTimeLeft(millisUntilFinished)
                        it.onTimeUpdate(millisUntilFinished, timeMilliParser.parseTimeInMilliSeconds(millisUntilFinished))
                    }
                }
            }
        }
        countDownTimer?.start()
    }

    fun showViewAdsNotification() {
        val viewMoreAdsIntent = Intent(this, VpnActivity::class.java)
        viewMoreAdsIntent.action = VpnActivity.VIEW_MORE_ADS_ACTION
        val viewMoreAdsPendingIntent = PendingIntent.getActivity(this, 0, viewMoreAdsIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val buyPremiumIntent = Intent(this, VpnActivity::class.java)
        buyPremiumIntent.action = VpnActivity.BUY_PREMIUM_ACTION
        val buyPremiumPendingIntent = PendingIntent.getActivity(this, 0, buyPremiumIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, EKO_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.eko_notifications)
                .setContentTitle(getString(R.string.out_of_time))
                .setContentText(getString(R.string.out_of_time_desc))
                .addAction(R.drawable.eko_notifications, getString(R.string.get_more_time), viewMoreAdsPendingIntent)
                .addAction(R.drawable.eko_notifications, getString(R.string.buy_premimum), buyPremiumPendingIntent)
                .setAutoCancel(true)

        val notification = builder.build()

        val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(TIMER_SERVICE_NOTIFICATION, notification)
    }

    fun disconnectCurrentVPN() {
        try {
            if (server is Server.IkeV2Server) {
                disconnectIKEv2()
            } else if (server is Server.OVPNServer) {
                disconnectOpenVPN()
            }
        } catch (e: Exception) {

            VpnStatus.logException(e)
            e.printStackTrace()
        }
    }

    private fun disconnectIKEv2() {
        val intent = Intent(this, VpnStateService::class.java)
        bindService(intent, object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder) {
                val vpnStateService = (service as LocalBinder).service
                if (vpnStateService.state == VpnStateService.State.CONNECTED ||
                        vpnStateService.state == VpnStateService.State.CONNECTING) {
                    vpnStateService.disconnect()
                }
            }
        }, Context.BIND_AUTO_CREATE)
    }

    private fun disconnectOpenVPN() {
        if (VpnStatus.isVPNActive()) {
            val intent = Intent(this, OpenVPNService::class.java)
            intent.action = OpenVPNService.START_SERVICE
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                    val service = IOpenVPNServiceInternal.Stub.asInterface(binder)
                    if (service != null) try {
                        service.stopVPN(false)
                    } catch (e: RemoteException) {
                        VpnStatus.logException(e)
                    }
                    unbindService(this)
                }

                override fun onServiceDisconnected(componentName: ComponentName) {}
            }, Context.BIND_AUTO_CREATE)
        }
    }


    override fun stopService(name: Intent?): Boolean {
        countDownTimer?.cancel()
        return super.stopService(name)
    }


    private fun runNotification() {

        // Compute some values required below.
        val base: Long = getChronometerBase()
        val pname: String = this.packageName

        val notificationIntent = Intent(this, VpnActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, EKO_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText("Connected")
                .setSmallIcon(R.drawable.eko_notifications)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntent)

        if (isNOrLater()) {
            notification.setCustomContentView(buildChronometer(pname, base, true, resources.getString(R.string.connected_status_)))
        } else {
            val contentText: CharSequence = resources.getString(R.string.connected_status_)
            notification.setContentText(contentText).setContentTitle(contentText)
        }
        val notificationBuild = notification.build()
        notificationBuild.flags = Notification.FLAG_AUTO_CANCEL
        startForeground(1, notificationBuild)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        disconnectCurrentVPN()
    }


    private fun getChronometerBase(): Long {
        val adjustedRemaining = if (timeLeft < 0) timeLeft else timeLeft + DateUtils.SECOND_IN_MILLIS
        return SystemClock.elapsedRealtime() + adjustedRemaining
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun buildChronometer(pname: String, base: Long, running: Boolean,
                                 stateText: CharSequence): RemoteViews? {
        val content = RemoteViews(pname, R.layout.chronometer_notif_content)
        content.setChronometerCountDown(R.id.chronometer, true)
        content.setChronometer(R.id.chronometer, base, null, running)
        content.setTextViewText(R.id.state, stateText)
        return content
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun stopTimer() {
        server = null
        countDownTimer?.cancel()
    }

    inner class VPNTimerLocalBinder : Binder() {
        fun getService(): EkoVPNMgrService = this@EkoVPNMgrService
    }

    interface TimeLeftListener {
        fun onTimeUpdate(timeLeftMillis: Long, timeLeftFormatted: String)
    }

    companion object {

        const val TIMER_SERVICE_VPN_PROFILE = "TIMER_SERVICE_VPN_PROFILE"
        const val TIMER_SERVICE_TIME_LEFT = "TIMER_SERVICE_TIME_LEFT"

        const val TIMER_SERVICE_INCREASE_TIME_LEFT_ACTION = "TIMER_SERVICE_INCREASE_TIME_LEFT_ACTION"
        const val TIMER_SERVICE_INCREMENT = "TIMER_SERVICE_INCREMENT"

        const val TIMER_GROUP = "TIMER_GROUP"
        const val TIMER_SERVICE_NOTIFICATION = 1234

    }
}
