/*
 * Copyright © 2017-2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ekovpn.wireguard.repo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ekovpn.wireguard.WireGuardInitializer.getAsyncWorker
import com.ekovpn.wireguard.WireGuardInitializer.getBackend
import com.ekovpn.wireguard.WireGuardInitializer.getSharedPreferences
import com.ekovpn.wireguard.WireGuardInitializer.getTunnelManager
import com.ekovpn.wireguard.configstore.ConfigStore
import com.ekovpn.wireguard.utils.ExceptionLoggers
import com.wireguard.android.backend.Statistics
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java9.util.concurrent.CompletableFuture
import java9.util.concurrent.CompletionStage
import kotlinx.coroutines.flow.*
import java.util.ArrayList

/**
 * Maintains and mediates changes to the set of available WireGuard tunnels,
 */
class TunnelManager constructor(private val configStore: ConfigStore, val context: Context) {
    private val delayedLoadRestoreTunnels = ArrayList<CompletableFuture<Void>>()
    private val tunnelMap: HashMap<String, EkoTunnel> = HashMap()
    private var haveLoaded = false

    private fun addToList(name: String, config: Config?, state: Tunnel.State): EkoTunnel? {
        val tunnel = EkoTunnel(this, name, config, state)
        tunnelMap[name] = tunnel
        return tunnel
    }

    fun create(name: String, config: Config?): EkoTunnel {
//        if (Tunnel.isNameInvalid(name))
//            throw (IllegalArgumentException("Tunnel $name is not valid"))
        if (tunnelMap.containsKey(name))
            throw (IllegalArgumentException("Tunnel $name is already existing"))

        val configuration = configStore.create(name, config!!)
        return (addToList(name, configuration, Tunnel.State.DOWN)!!)
    }

    fun delete(tunnel: EkoTunnel): Flow<Unit> {
        return flow {
            val originalState = tunnel.state
            val wasLastUsed = tunnel == lastUsedTunnel
            // Make sure nothing touches the tunnel.
            if (wasLastUsed)
                lastUsedTunnel = null
            tunnelMap.remove(tunnel.name)
            if (originalState == Tunnel.State.UP)
                getBackend().setState(tunnel, Tunnel.State.DOWN, null)
            try {
                configStore.delete(tunnel.name)
            } catch (e: Exception) {
                if (originalState == Tunnel.State.UP)
                    getBackend().setState(tunnel, Tunnel.State.UP, tunnel.config)
                tunnelMap[tunnel.name] = tunnel
                if (wasLastUsed)
                    lastUsedTunnel = tunnel
                throw e
            }
            emit(Unit)
        }
    }

    var lastUsedTunnel: EkoTunnel? = null
        private set(value) {
            if (value == field) return
            field = value
            if (value != null)
                getSharedPreferences().edit().putString(KEY_LAST_USED_TUNNEL, value.name).commit()
            else
                getSharedPreferences().edit().remove(KEY_LAST_USED_TUNNEL).commit()
        }

    fun getTunnelConfig(tunnel: EkoTunnel): Flow<Config> = flow {
        emit(tunnel.onConfigChanged(configStore.load(tunnel.name))!!)
    }


    fun onCreate() {
        getAsyncWorker().supplyAsync { configStore.enumerate() }
                .thenAcceptBoth(getAsyncWorker().supplyAsync { getBackend().runningTunnelNames }, this::onTunnelsLoaded)
                .whenComplete(ExceptionLoggers.E)
    }

    private fun onTunnelsLoaded(present: Iterable<String>, running: Collection<String>) {
        for (name in present)
            addToList(name, null, if (running.contains(name)) Tunnel.State.UP else Tunnel.State.DOWN)
        val lastUsedName = getSharedPreferences().getString(KEY_LAST_USED_TUNNEL, null)
        if (lastUsedName != null)
            lastUsedTunnel = tunnelMap[lastUsedName]
        var toComplete: Array<CompletableFuture<Void>>
        synchronized(delayedLoadRestoreTunnels) {
            haveLoaded = true
            toComplete = delayedLoadRestoreTunnels.toTypedArray()
            delayedLoadRestoreTunnels.clear()
        }
    }

    fun refreshTunnelStates() {
        getAsyncWorker().supplyAsync { getBackend().runningTunnelNames }
                .thenAccept { running: Set<String> -> for (tunnel in tunnelMap) tunnel.value.onStateChanged(if (running.contains(tunnel.value.name)) Tunnel.State.UP else Tunnel.State.DOWN) }
                .whenComplete(ExceptionLoggers.E)
    }


    @SuppressLint("ApplySharedPref")
    fun saveState() {
        getSharedPreferences().edit().putStringSet(KEY_RUNNING_TUNNELS, tunnelMap.filter { it.value.state == Tunnel.State.UP }.map { it.value.name }.toSet()).commit()
    }

    fun setTunnelConfig(tunnel: EkoTunnel, config: Config): Flow<Config> = flow {
        getBackend().setState(tunnel, tunnel.state, config)
        configStore.save(tunnel.name, config)
        emit(tunnel.onConfigChanged(config)!!)
    }

    fun setTunnelName(tunnel: EkoTunnel, name: String): Flow<String> {
        return flow {
            if (Tunnel.isNameInvalid(name))
                throw (IllegalArgumentException("Tunnel name is not valid"))
            if (tunnelMap.containsKey(name)) {
                throw (IllegalArgumentException("Tunnel $name is already existing"))
            }
            val originalState = tunnel.state
            val wasLastUsed = tunnel == lastUsedTunnel
            // Make sure nothing touches the tunnel.
            if (wasLastUsed)
                lastUsedTunnel = null
            tunnelMap.remove(tunnel.name)

            try {
                if (originalState == Tunnel.State.UP)
                    getBackend().setState(tunnel, Tunnel.State.DOWN, null)
                configStore.rename(tunnel.name, name)
                val newName = tunnel.onNameChanged(name)
                if (originalState == Tunnel.State.UP)
                    getBackend().setState(tunnel, Tunnel.State.UP, tunnel.config)
                emit(newName)
            }catch (e:Exception){
                getTunnelState(tunnel)
                // Add the tunnel back to the manager, under whatever name it thinks it has.
                tunnelMap[tunnel.name] = tunnel
                if (wasLastUsed)
                    lastUsedTunnel = tunnel
                throw e
            }
        }
    }

    fun setTunnelState(tunnel: EkoTunnel, state: Tunnel.State): Flow<Tunnel.State> = tunnel.configAsync
            .map { getBackend().setState(tunnel, state, it)}
            .onCompletion { e ->
                // Ensure onStateChanged is always called (failure or not), and with the correct state.
                tunnel.onStateChanged(if (e == null) state else tunnel.state)
                if (e == null && state == Tunnel.State.UP)
                    lastUsedTunnel = tunnel
                saveState()
                emit(state)
            }

    class IntentReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val manager = getTunnelManager()
            if (intent == null) return
            val action = intent.action ?: return
            if ("com.wireguard.android.action.REFRESH_TUNNEL_STATES" == action) {
                manager.refreshTunnelStates()
                return
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !getSharedPreferences().getBoolean("allow_remote_control_intents", false))
                return
            val state: Tunnel.State
            state = when (action) {
                "com.wireguard.android.action.SET_TUNNEL_UP" -> Tunnel.State.UP
                "com.wireguard.android.action.SET_TUNNEL_DOWN" -> Tunnel.State.DOWN
                else -> return
            }
            val tunnelName = intent.getStringExtra("tunnel") ?: return
            manager.tunnelMap.let {
                val tunnel = it[tunnelName] ?: return
                manager.setTunnelState(tunnel, state)
            }
        }
    }

    fun getTunnelState(tunnel: EkoTunnel): Flow<Tunnel.State> = flow {
        val state = getBackend().getState(tunnel)
        emit(tunnel.onStateChanged(state))
    }

    fun getTunnelStatistics(tunnel: EkoTunnel): Flow<Statistics> = flow{
        val stats = getBackend().getStatistics(tunnel)
        emit(tunnel.onStatisticsChanged(stats)!!)
    }

    companion object {
        private const val KEY_LAST_USED_TUNNEL = "last_used_tunnel"
        private const val KEY_RESTORE_ON_BOOT = "restore_on_boot"
        private const val KEY_RUNNING_TUNNELS = "enabled_configs"
    }
}
