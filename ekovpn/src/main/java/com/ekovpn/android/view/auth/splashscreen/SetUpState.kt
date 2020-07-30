/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.splashscreen


sealed class SetUpState(
    val isLoading: Boolean,
    val error: Throwable?
) {

    object Idle : SetUpState(true, null)
    object Working : SetUpState(true, null)
    object Finished : SetUpState(false, null)
    object Failed : SetUpState(false, Throwable("An error occurred"))

}