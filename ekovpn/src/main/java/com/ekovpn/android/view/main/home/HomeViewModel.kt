/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.servers.ServersRepository
import com.ekovpn.android.data.repositories.user.UserRepository
import com.ekovpn.android.models.Server
import com.ekovpn.android.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(private val serversRepository: ServersRepository,
                                        private val userRepository: UserRepository) : ViewModel() {


    suspend fun getOVPNProfileForServer(profileUUID: String): de.blinkt.openvpn.VpnProfile? {
        return serversRepository.getOVPNProfileForServer(profileUUID)
    }

    suspend fun getIKEv2ProfileForServer(id: Long): org.strongswan.android.data.VpnProfile? {
        return serversRepository.getIKEv2ProfileForServer(id)
    }

    fun shouldShowAds(): Boolean {
        return state.value.user?.account_type != User.AccountType.PAID
    }

    fun connectingToServer(server: Server) {
        _state.value = state.value.copy(currentConnectionServer = server, connectionStatus = HomeState.ConnectionStatus.CONNECTING)
    }

    fun setDisconnected() {
        _state.value = state.value.copy(currentConnectionServer = null, connectionStatus = HomeState.ConnectionStatus.DISCONNECTED)
        saveLastUsedLocation()
        fetchTimeLeft()
    }


    fun setConnected() {
        saveLastUsedLocation()
        _state.value = state.value.copy(connectionStatus = HomeState.ConnectionStatus.CONNECTED, hasShownBalloonCTA = true)
    }

    private fun saveLastUsedLocation() {
        val lastUsedServer = if (state.value.currentConnectionServer != null) {
            state.value.currentConnectionServer
        } else {
            state.value.lastUsedServer
        }
        _state.value = state.value.copy(lastUsedServer = lastUsedServer)
        state.value.lastUsedServer?.let {
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


        userRepository.streamCurrentUser()
                .catch {
                    _state.value = _state.value.copy(_error = it)
                }
                .onEach {
                    _state.value = _state.value.copy(user = it)
                }.launchIn(viewModelScope)
    }

    fun fetchServersForCurrentProtocol() {
        serversRepository.getServersForCurrentProtocol()
                .catch {
                    it.printStackTrace()
                    _state.value = _state.value.copy(_error = it)
                }
                .onEach {
                    val sortedList = it.sortedBy { server ->
                        server.location_.country
                    }
                    val lastUsed = if (_state.value.lastUsedServer == null) {
                        sortedList.firstOrNull()
                    } else {
                        _state.value.lastUsedServer
                    }
                    _state.value = _state.value.copy(_serversList = sortedList, lastUsedServer = lastUsed)
                }.launchIn(viewModelScope)
    }

    fun fetchTimeLeft() {
        _state.value = state.value.copy(connectionStatus = state.value.connectionStatus, timeLeft = userRepository.getTimeLeft())
    }

    fun storeTimeLeft(timeLeftMillis: Long) {
        userRepository.setTimeLeft(timeLeftMillis)
    }

}