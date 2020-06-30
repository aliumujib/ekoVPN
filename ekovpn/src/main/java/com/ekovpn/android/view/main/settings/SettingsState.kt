/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.settings

import com.ekovpn.android.data.config.model.Protocol


sealed class SettingsState(
        val isLoading: Boolean,
        val selectedProtocol: Protocol,
        val error: Throwable?
) {

    data class Init(val protocol: Protocol) : SettingsState(true, protocol, null)
    data class Idle(val protocol: Protocol) : SettingsState(false, protocol, null)

}