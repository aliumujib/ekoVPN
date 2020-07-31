/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.success

import com.ekovpn.android.models.User


data class SuccessState(
        val isLoading: Boolean,
        val user: User?,
        val isNewUser:Boolean,
        val error: Throwable?
)