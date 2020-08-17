/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.repositories.ads.AdsRepository
import com.ekovpn.android.data.repositories.user.UserRepository
import com.ekovpn.android.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AdsViewModel @Inject constructor(adsRepository: AdsRepository,
                                       private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(AdsState(timeLeft = userRepository.getTimeLeft()))
    val state: StateFlow<AdsState> = _state

    fun shouldViewAds(): Boolean {
        return state.value.user?.account_type != User.AccountType.PAID
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

    init {
        adsRepository.fetchAds()
                .onEach {
                    _state.value = _state.value.copy(ads = it)
                }.catch {
                    it.printStackTrace()
                    _state.value = _state.value.copy(error = it)
                }.launchIn(viewModelScope)

        userRepository.streamCurrentUser()
                .onEach {
                    _state.value = _state.value.copy(user = it)
                }.catch {
                    it.printStackTrace()
                    _state.value = _state.value.copy(error = it)
                }.launchIn(viewModelScope)
    }

    fun saveAddedTime(addedTime: Long) {
        userRepository.addToTimeLeft(addedTime)
        _state.value = state.value.copy(timeLeft = userRepository.getTimeLeft())
    }

}