/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.servers.ServersRepository
import com.ekovpn.android.models.Protocol
import com.ekovpn.android.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(private val serversRepository: ServersRepository) : ViewModel() {


//    private val _state = MutableStateFlow<SettingsState>(SettingsState.Init(settingsRepository.getSelectedProtocol()))
//    val state: StateFlow<SettingsState> = _state

    init {
        serversRepository.getServersForCurrentProtocol()
                .onEach {
                    it.forEach {
                        Log.d("AAA", it.toString())
                    }
                }.launchIn(viewModelScope)
    }

}