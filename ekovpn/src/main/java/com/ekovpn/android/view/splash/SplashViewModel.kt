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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SplashViewModel @Inject constructor(private val configRepository: ConfigRepository, private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<SetUpState>(SetUpState.Idle)
    val state: StateFlow<SetUpState> = _state

    init {

        authRepository.createAccount().onEach {
            Log.d(SplashViewModel::class.java.simpleName, "$it")
            runSetupIfNeeded()
        }.catch {
            it.printStackTrace()
        }.launchIn(viewModelScope)
    }

    fun runSetupIfNeeded() {
        if (configRepository.hasConfiguredServers().not()) {
            configRepository.fetchAndConfigureServers()
                    .flowOn(Dispatchers.IO)
                    .onStart {
                        _state.value = SetUpState.Working
                    }
                    .onEach {
                        if (it.isSuccess) {
                            Log.d(SplashViewModel::class.java.simpleName, "Success")
                        } else {
                            _state.value = SetUpState.Failed
                            Log.d(SplashViewModel::class.java.simpleName, "Error")
                        }
                    }.onCompletion {
                        if (it != null) {
                            _state.value = SetUpState.Failed
                        } else {
                            _state.value = SetUpState.Finished
                        }
                    }.catch {
                        Log.d(SplashViewModel::class.java.simpleName, "${it.message}")
                        _state.value = SetUpState.Failed
                    }
                    .launchIn(viewModelScope)
        } else {
            _state.value = SetUpState.Finished
        }
    }

}