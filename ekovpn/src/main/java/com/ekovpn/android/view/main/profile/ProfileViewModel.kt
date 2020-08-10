/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.config.repository.ConfigRepository
import com.ekovpn.android.data.repositories.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ProfileViewModel @Inject constructor(val userRepository: UserRepository, private val configRepository: ConfigRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState(true, null, null, false))
    val state: StateFlow<ProfileState> = _state

    fun updateUserWithOrderId(orderId: String) {
        userRepository.updateUserWithOrderId(orderId)
                .onStart {
                    _state.value = _state.value.copy(isLoading = true)
                }.catch {
                    _state.value = _state.value.copy(error = it)
                }
                .onEach {
                    _state.value = _state.value.copy(user = it, isLoading = false)
                }.launchIn(viewModelScope)
    }

    fun logOut() {
        configRepository.logOutAndClearData().onStart {
            _state.value = _state.value.copy(isLoading = true)
        }.catch {
            _state.value = _state.value.copy(error = it)
        }.onEach {
            _state.value = _state.value.copy(user = null, isLoading = false, isLoggedOut = true)
        }
                .launchIn(viewModelScope)
    }

    init {
        userRepository.getCurrentUser()
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