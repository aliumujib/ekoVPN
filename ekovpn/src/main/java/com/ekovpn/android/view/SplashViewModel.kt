package com.ekovpn.android.view

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekovpn.android.data.config.repository.ConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SplashViewModel @Inject constructor(private val configRepository: ConfigRepository) : ViewModel() {

    private val _state = MutableStateFlow<SetUpState>(SetUpState.Idle)
    val state: StateFlow<SetUpState> = _state

    init {
        if(configRepository.hasConfiguredServers().not()){
            configRepository.fetchAndConfigureServers()
                    .flowOn(Dispatchers.IO)
                    .onStart {
                        _state.value = SetUpState.Working
                    }
                    .onEach {
                        if (it.isSuccess) {
                            Log.d(SplashActivity::class.java.simpleName, "Success")
                        } else {
                            _state.value = SetUpState.Failed
                            Log.d(SplashActivity::class.java.simpleName, "Error")
                        }
                    }.onCompletion {
                        _state.value = SetUpState.Finished
                    }
                    .launchIn(viewModelScope)
        }else{
            _state.value = SetUpState.Finished
        }
    }

}