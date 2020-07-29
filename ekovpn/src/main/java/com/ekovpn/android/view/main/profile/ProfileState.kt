/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.profile

import com.ekovpn.android.models.User


data class ProfileState(
        val isLoading: Boolean,
        val user: User?,
        val error: Throwable?
) {
//
//    object Idle : ProfileState(true, null, null)
//    object Working : ProfileState(true, null, null)
//    data class Finished(val user: User) : ProfileState(false, user, null)
//    object Failed : ProfileState(false, null, Throwable("An error occurred"))

}