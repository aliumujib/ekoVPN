/*
 * Copyright © 2017-2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ekovpn.wireguard.repo

import com.ekovpn.wireguard.utils.ExceptionLoggers
import com.wireguard.android.backend.Statistics
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion

/**
 * Encapsulates the volatile and nonvolatile state of a WireGuard tunnel.
 */
class EkoTunnel internal constructor(
        private val manager: TunnelManager,
        private var name: String,
        config: Config?,
        state: Tunnel.State
) : Tunnel {

    override fun getName() = name

    fun setNameAsync(name: String): Flow<String> {
        return if (name != this@EkoTunnel.name)
            manager.setTunnelName(this@EkoTunnel, name)
        else
            flowOf((this@EkoTunnel.name))
    }

    fun onNameChanged(name: String): String {
        this.name = name
        return name
    }


    var state = state
        private set

    override fun onStateChange(newState: Tunnel.State) {
        onStateChanged(newState)
    }

    fun onStateChanged(state: Tunnel.State): Tunnel.State {
        if (state != Tunnel.State.UP) onStatisticsChanged(null)
        this.state = state
        return state
    }

    fun setStateAsync(state: Tunnel.State): Flow<Tunnel.State> {
        return if (state != this@EkoTunnel.state)
            manager.setTunnelState(this@EkoTunnel, state)
        else
            flowOf(this@EkoTunnel.state)
    }


    var config = config
        get() {
            if (field == null)
                manager.getTunnelConfig(this)
            return field
        }
        private set

    val configAsync: Flow<Config>
        get() = if (this@EkoTunnel.config == null)
            manager.getTunnelConfigAsync(this)
        else
            flowOf(config!!)

    fun setConfig(newConfig: Config){
        manager.setTunnelConfigSync(this, newConfig)
    }

    fun setConfigAsync(newConfig: Config): Flow<Config> {
        return if (newConfig != this@EkoTunnel.config)
            manager.setTunnelConfig(this, newConfig)
        else
            flowOf(this@EkoTunnel.config!!)
    }

    fun onConfigChanged(config: Config?): Config? {
        this.config = config
        return config
    }


    var statistics: Statistics? = null
        get() {
            if (field == null || field?.isStale != false)
                manager.getTunnelStatistics(this).onCompletion { ExceptionLoggers.E}
            return field
        }
        private set

    val statisticsAsync: Flow<Statistics> = if (statistics == null || statistics?.isStale != false)
            manager.getTunnelStatistics(this)
        else
            flowOf(statistics!!)


    fun onStatisticsChanged(statistics: Statistics?): Statistics? {
        this@EkoTunnel.statistics = statistics
        return statistics
    }

    fun delete(): Flow<Unit> = manager.deleteAsync(this)
}
