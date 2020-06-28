package com.ekovpn.android.view


sealed class SetUpState(
    val isLoading: Boolean,
    val error: Throwable?
) {

    object Idle : SetUpState(true, null)
    object Working : SetUpState(true, null)
    object Finished : SetUpState(false, null)
    object Failed : SetUpState(false, Throwable("An error occurred"))

}