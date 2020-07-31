
/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.success

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SuccessViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {



    private val _state = MutableStateFlow(SuccessState(true, null, false, null))
    val state: StateFlow<SuccessState> = _state

    init {
        userRepository.getCurrentUser().onEach {
            _state.value = _state.value.copy(user = it, isLoading = false)
        }.launchIn(viewModelScope)
    }

    fun setIsFreshAccount(freshAccount: Boolean) {
        _state.value = _state.value.copy(isNewUser = freshAccount)
    }

}