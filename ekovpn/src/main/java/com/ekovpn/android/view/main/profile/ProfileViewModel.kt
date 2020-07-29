/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ProfileViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState(true, null, null))
    val state: StateFlow<ProfileState> = _state

    init {
        userRepository.getCurrentUser().onEach {
            _state.value = _state.value.copy(user = it, isLoading = false)
        }.launchIn(viewModelScope)
    }

}