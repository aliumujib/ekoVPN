/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.auth.AuthRepository
import com.ekovpn.android.data.repositories.config.repository.ConfigRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class LoginViewModel @Inject constructor(private val configRepository: ConfigRepository, private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state


    private fun fetchServers() {
        configRepository.fetchAndConfigureServers()
                .onStart {
                    _state.value = LoginState.Working
                }
                .onEach {
                    if (it.isSuccess) {
                        Log.d(LoginViewModel::class.java.simpleName, "Success")
                    } else {
                        _state.value = LoginState.Failed()
                        Log.d(LoginViewModel::class.java.simpleName, "Error")
                    }
                }.onCompletion {
                    if (it != null) {
                        _state.value = LoginState.Failed()
                    } else {
                        _state.value = LoginState.Finished
                    }
                }.catch {
                    Log.d(LoginViewModel::class.java.simpleName, "${it.message}")
                    _state.value = LoginState.Failed()
                }
                .launchIn(viewModelScope)
    }

    fun login(accountNumber: String) {
        if (accountNumber.isEmpty()) {
            _state.value = LoginState.Failed(Throwable("Please enter your account number"))
            return
        }
        authRepository.fetchUserByAccountNumber(accountNumber)
                .onStart {
                    _state.value = LoginState.Working
                }
                .onEach {
                    Log.d(LoginViewModel::class.java.simpleName, "$it")
                    fetchServers()
                }.catch {
                    it.printStackTrace()
                    _state.value = LoginState.Failed(it)
                }.launchIn(viewModelScope)
    }


    fun createAccount() {
        authRepository.createAccount().onEach {
            Log.d(LoginViewModel::class.java.simpleName, "$it")
            fetchServers()
        }.onStart {
            _state.value = LoginState.Working
        }.catch {
            it.printStackTrace()
            _state.value = LoginState.Failed()
        }.launchIn(viewModelScope)
    }

}