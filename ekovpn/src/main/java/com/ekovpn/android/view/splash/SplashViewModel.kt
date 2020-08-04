/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.auth.AuthRepository
import com.ekovpn.android.data.repositories.config.repository.ConfigRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SplashViewModel @Inject constructor(private val configRepository: ConfigRepository, private val authRepository: AuthRepository) : ViewModel() {

    private val  _state = MutableStateFlow<SetUpState>(SetUpState.Idle)
    val state: StateFlow<SetUpState> = _state

    init {
        runSetupIfNeeded()
    }

    fun runSetupIfNeeded() {
        if (configRepository.hasConfiguredServers().not()) {
            login()
        } else {
            _state.value = SetUpState.Finished
        }
    }



    private fun login() {
        authRepository.loginToApp()
                .onStart {

                }
                .onEach {
                    Log.d(SplashViewModel::class.java.simpleName, "$it")
                    _state.value = SetUpState.LoggedIn
                }.catch {
                    it.printStackTrace()
                    _state.value = SetUpState.Failed
                }.launchIn(viewModelScope)
    }

}