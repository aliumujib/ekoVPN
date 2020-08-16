/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.settings

import com.ekovpn.android.models.Protocol
import com.ekovpn.android.models.User


data class SettingsState(
        val isLoading: Boolean = false,
        val selectedProtocol: Protocol,
        val error: Throwable?,
        val user: User? = null
)