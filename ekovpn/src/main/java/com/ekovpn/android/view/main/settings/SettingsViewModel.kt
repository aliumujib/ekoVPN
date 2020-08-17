/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.models.Protocol
import com.ekovpn.android.data.repositories.settings.SettingsRepository
import com.ekovpn.android.data.repositories.user.UserRepository
import com.ekovpn.android.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SettingsViewModel @Inject constructor(private val userRepository: UserRepository, private val settingsRepository: SettingsRepository) : ViewModel() {

    fun selectProtocol(protocol: Protocol) {
        settingsRepository.setSelectedProtocol(protocol)
    }

    fun shouldShowAds(): Boolean {
        return state.value.user?.account_type != User.AccountType.PAID
    }


    private val _state = MutableStateFlow<SettingsState>(SettingsState(selectedProtocol = settingsRepository.getSelectedProtocol(), error = null))
    val state: StateFlow<SettingsState> = _state

    init {
        userRepository.streamCurrentUser()
                .onStart {
                    _state.value = _state.value.copy(isLoading = true)
                }.catch {
                    _state.value = _state.value.copy(error = it)
                }
                .onEach {
                    _state.value = _state.value.copy(user = it, isLoading = false)
                }.launchIn(viewModelScope)
    }

}