/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.auth.AuthRepository
import com.ekovpn.android.data.repositories.config.repository.ConfigRepository
import com.ekovpn.android.data.repositories.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AuthViewModel @Inject constructor(private val configRepository: ConfigRepository,
                                        private val userRepository: UserRepository,
                                        private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState(false, null, null, false, false))
    val state: StateFlow<AuthState> = _state


    private val _navigation = MutableStateFlow<NavCommand>(NavCommand.Stay)
    val navigation: StateFlow<NavCommand> = _navigation

    init {
        userRepository.streamCurrentUser().onEach {
            _state.value = _state.value.copy(user = it, isLoading = false)
        }.launchIn(viewModelScope)
    }

    fun fetchAccountId():String?{
        return _state.value.user?.account_id
    }

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

    private fun fetchServers(isFreshAccount: Boolean) {
        configRepository.fetchAndConfigureServers()
                .onStart {
                    _state.value = _state.value.copy(isLoading = true)
                }
                .onEach {
                    if (it.isSuccess) {
                        Log.d(AuthViewModel::class.java.simpleName, "Success")
                    } else {
                        _state.value = _state.value.copy(error = Throwable("An error occurred"), isLoading = false, user = null)
                    }
                }.onCompletion {error->
                    Log.d(AuthViewModel::class.java.simpleName, "$error")
                    if (error != null) {
                        error.printStackTrace()
                        _state.value = _state.value.copy(error = Throwable("An error occurred"), isLoading = false, user = null)
                    } else {
                        configRepository.markSetupAsComplete()
                        _state.value = _state.value.copy( isLoading = false, isFreshAccount = isFreshAccount, hasCompletedConfig = true)
                    }
                }.catch {
                    Log.d(AuthViewModel::class.java.simpleName, "${it.message}")
                    _state.value = _state.value.copy(error = Throwable("An error occurred"), isLoading = false, user = null)
                }
                .launchIn(viewModelScope)
    }

    fun login(accountNumber: String) {
        if (accountNumber.isEmpty()) {
            _state.value = _state.value.copy(error = Throwable("Please enter your account number"), isLoading = false, user = null)
            return
        }
        authRepository.fetchUserByAccountNumber(accountNumber.trim().replace(" ",""))
                .onStart {
                    _state.value = _state.value.copy(isLoading = true)
                }
                .onEach {
                    fetchServers(false)
                }.catch {
                    it.printStackTrace()
                    _state.value = _state.value.copy(error = Throwable("An error occurred, while logging you in, please retry"), isLoading = false, user = null)
                }.launchIn(viewModelScope)
    }


    fun createAccount() {
        authRepository.createAccount().onEach {
            fetchServers(true)
        }.onStart {
            _state.value = _state.value.copy(isLoading = true)
        }.catch {
            it.printStackTrace()
            _state.value = _state.value.copy(error = Throwable("An error occurred while signing you up, please retry"), isLoading = false, user = null)
        }.launchIn(viewModelScope)
    }

    fun recoverAccount(orderNumber: String) {
        authRepository.fetchUserByOrderNumber(orderNumber.trim()).onEach {
            fetchServers(true)
        }.onStart {
            _state.value = _state.value.copy(isLoading = true)
        }.catch {
            it.printStackTrace()
            _state.value = _state.value.copy(error = Throwable("An error occurred while recovering your account, please contact support."), isLoading = false, user = null)
        }.launchIn(viewModelScope)
    }


    fun goToSuccessScreen() {
        _navigation.value = NavCommand.GoToSuccessCommand
    }

    fun applyRefferalCode(referralCode: String) {
        authRepository.redeemReferral(referralCode.trim()).onStart {
            _state.value = _state.value.copy(isLoading = true)
        }.catch {
            it.printStackTrace()
            _state.value = _state.value.copy(error = Throwable("An error occurred while recovering your account, please contact support."), isLoading = false, user = null)
        }.launchIn(viewModelScope)
    }

}