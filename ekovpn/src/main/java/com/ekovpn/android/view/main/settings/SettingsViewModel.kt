/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.settings

import androidx.lifecycle.ViewModel
import com.ekovpn.android.data.config.model.Protocol
import com.ekovpn.android.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SettingsViewModel @Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {

    fun selectProtocol(protocol: Protocol) {
        settingsRepository.setSelectedProtocol(protocol)
    }

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Init(settingsRepository.getSelectedProtocol()))
    val state: StateFlow<SettingsState> = _state

    init {

    }

}