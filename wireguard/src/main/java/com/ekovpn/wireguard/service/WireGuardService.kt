/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.wireguard.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.ekovpn.wireguard.R
import com.ekovpn.wireguard.WireGuardInitializer
import com.ekovpn.wireguard.repo.EkoTunnel
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WireGuardService : Service() {

    private val listeners = mutableSetOf<WireGuardListener>()
    private var currentTunnel: EkoTunnel? = null
    private val binder = WireGuardServiceLocalBinder()
    private var retryCount = 0

    inner class WireGuardServiceLocalBinder : Binder() {
        fun getService(): WireGuardService = this@WireGuardService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun runNotification() {
        val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, EKO_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText("Connected")
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        val notificationBuild = notification.build()
        notificationBuild.flags = Notification.FLAG_AUTO_CANCEL
        startForeground(20, notificationBuild)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tunnelName = intent?.getStringExtra(TUNNEL_NAME)
        tunnelName?.let {
            connectToTunnelWithName(it)
        }
        return START_NOT_STICKY
    }

    public fun disconnect() {
        currentTunnel?.let {
            setTunnelStateWithPermissionsResult(false)
            currentTunnel = null
        }
        stopForeground(true)
    }

     fun getCurrentState(): Tunnel.State {
        return currentTunnel?.state ?: Tunnel.State.DOWN
    }

    private fun connectToTunnelWithName(tunnelName: String) {
        currentTunnel = WireGuardInitializer.getTunnelManager().getTunnel(tunnelName)
        setTunnelStateWithPermissionsResult(true)
    }

    private fun setTunnelStateWithPermissionsResult(checked: Boolean) {
        currentTunnel
                ?.setStateAsync(Tunnel.State.of(checked))
                ?.onEach { state ->
                    if (state == Tunnel.State.UP) {
                        runNotification()
                    }
                    listeners.forEach {
                        it.onStateChange(state)
                    }
                    retryCount = 0
                }?.catch { throwable ->
                    throwable.printStackTrace()
                    if(retryCount < 4){
                        retryCount += 1
                        setTunnelStateWithPermissionsResult(checked)
                    }
                    Toast.makeText(applicationContext, "An error occurred", Toast.LENGTH_LONG).show()
                }?.launchIn(GlobalScope)
    }

    fun registerListener(listener: WireGuardListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: WireGuardListener) {
        listeners.remove(listener)
    }

    interface WireGuardListener {
        fun onStateChange(status: Tunnel.State)
    }

    companion object {
        const val CONNECT_TO_VPN = "CONNECT_TO_VPN"
        const val TUNNEL_NAME = "TUNNEL_NAME"
        const val DISCONNECT_FROM_VPN = "DISCONNECT_FROM_VPN"

        const val EKO_NOTIFICATION_CHANNEL_NAME = "EKO_NOTIFICATION_CHANNEL_NAME"
        const val EKO_NOTIFICATION_CHANNEL_ID = "EKO_NOTIFICATION_CHANNEL_ID"
    }

}