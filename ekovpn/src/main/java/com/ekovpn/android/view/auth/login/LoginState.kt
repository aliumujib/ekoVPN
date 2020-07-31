/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.login


sealed class LoginState(
        val isLoading: Boolean,
        error: Throwable?
) {

    object Idle : LoginState(true, null)
    object Working : LoginState(true, null)
    object Finished : LoginState(false, null)
    data class Failed(val error: Throwable = Throwable("An error occurred")) : LoginState(false, error)

}