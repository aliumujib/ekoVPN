/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.servers.ServersRepository
import com.ekovpn.android.models.Server
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(private val serversRepository: ServersRepository) : ViewModel() {

    fun connectingToServer(server: Server) {
        _state.value = state.value.copy(currentConnectionServer = server, connectionStatus = HomeState.ConnectionStatus.CONNECTING)
    }

    fun setDisconnected() {
        saveLastUsedLocation()
        _state.value = state.value.copy(connectionStatus = HomeState.ConnectionStatus.DISCONNECTED)
    }

    fun setConnected() {
        saveLastUsedLocation()
        _state.value = state.value.copy(connectionStatus = HomeState.ConnectionStatus.CONNECTED)
    }

    fun saveLastUsedLocation() {
        _state.value = state.value.copy(lastUsedServer = state.value.currentConnectionServer)
        state.value.currentConnectionServer?.let {
            serversRepository.saveLastUsedServer(it.id_)
        }
    }


    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {

        serversRepository.getServersForCurrentProtocol()
                .onEach {
                    _state.value = _state.value.copy(_serversList = it)
                }.launchIn(viewModelScope)

        serversRepository.getLastUsedLocation()
                .onEach {
                    _state.value = _state.value.copy(lastUsedServer = it)
                }.launchIn(viewModelScope)
    }

}