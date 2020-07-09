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
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(private val serversRepository: ServersRepository) : ViewModel() {


    suspend fun getOVPNProfileForServer(profileUUID: String): de.blinkt.openvpn.VpnProfile? {
        return serversRepository.getOVPNProfileForServer(profileUUID)
    }

    suspend fun getIKEv2ProfileForServer(id: Long): org.strongswan.android.data.VpnProfile? {
        return serversRepository.getIKEv2ProfileForServer(id)
    }

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

    private fun saveLastUsedLocation() {
        _state.value = state.value.copy(lastUsedServer = state.value.currentConnectionServer)
        state.value.currentConnectionServer?.let {
            serversRepository.saveLastUsedServer(it.id_)
        }
    }

    fun fetchLocationForCurrentIP() {
        serversRepository.getCurrentLocation()
                .catch {
                    _state.value = _state.value.copy(_error = it)
                }
                .onEach {
                    _state.value = _state.value.copy(currentLocation = it)
                }
                .launchIn(viewModelScope)
    }


    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        fetchServersForCurrentProtocol()

        serversRepository.getLastUsedLocation()
                .catch {
                    _state.value = _state.value.copy(_error = it)
                }
                .onEach {
                    _state.value = _state.value.copy(lastUsedServer = it)
                }.launchIn(viewModelScope)
    }

    fun fetchServersForCurrentProtocol() {
        serversRepository.getServersForCurrentProtocol()
                .catch {
                    it.printStackTrace()
                    _state.value = _state.value.copy(_error = it)
                }
                .onEach {
                    _state.value = _state.value.copy(_serversList = it)
                }.launchIn(viewModelScope)
    }

}