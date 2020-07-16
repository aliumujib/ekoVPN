/*
 * Copyright © 2020 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import com.wireguard.android.WireGuardInitializer
import com.wireguard.android.R
import com.wireguard.android.activity.SettingsActivity
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.WgQuickBackend
import java9.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class KernelModuleDisablerPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {
    private var state = State.UNKNOWN

    init {
        isVisible = false
        WireGuardInitializer.getBackendAsync().thenAccept { backend ->
            setState(if (backend is WgQuickBackend) State.ENABLED else State.DISABLED)
        }
    }

    override fun getSummary() = if (state == State.UNKNOWN) "" else context.getString(state.summaryResourceId)

    override fun getTitle() = if (state == State.UNKNOWN) "" else context.getString(state.titleResourceId)

    @SuppressLint("ApplySharedPref")
    override fun onClick() {
        if (state == State.DISABLED) {
            setState(State.ENABLING)
            WireGuardInitializer.getSharedPreferences().edit().putBoolean("disable_kernel_module", false).commit()
        } else if (state == State.ENABLED) {
            setState(State.DISABLING)
            WireGuardInitializer.getSharedPreferences().edit().putBoolean("disable_kernel_module", true).commit()
        }
        WireGuardInitializer.getAsyncWorker().runAsync {
            WireGuardInitializer.getTunnelManager().tunnels.thenApply { observableTunnels ->
                val downings = observableTunnels.map { it.setStateAsync(Tunnel.State.DOWN).toCompletableFuture() }.toTypedArray()
                CompletableFuture.allOf(*downings).thenRun {
                    val restartIntent = Intent(context, SettingsActivity::class.java)
                    restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    WireGuardInitializer.get().startActivity(restartIntent)
                    exitProcess(0)
                }
            }.join()
        }
    }

    private fun setState(state: State) {
        if (this.state == state) return
        this.state = state
        if (isEnabled != state.shouldEnableView) isEnabled = state.shouldEnableView
        if (isVisible != state.visible) isVisible = state.visible
        notifyChanged()
    }

    private enum class State(val titleResourceId: Int, val summaryResourceId: Int, val shouldEnableView: Boolean, val visible: Boolean) {
        UNKNOWN(0, 0, false, false),
        ENABLED(R.string.module_disabler_enabled_title, R.string.module_disabler_enabled_summary, true, true),
        DISABLED(R.string.module_disabler_disabled_title, R.string.module_disabler_disabled_summary, true, true),
        ENABLING(R.string.module_disabler_disabled_title, R.string.success_application_will_restart, false, true),
        DISABLING(R.string.module_disabler_enabled_title, R.string.success_application_will_restart, false, true);
    }
}
