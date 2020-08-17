/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.ads

import com.ekovpn.android.models.Ad
import com.ekovpn.android.models.User


data class AdsState(
        val timeLeft: Long = 0L,
        val isLoading: Boolean = false,
        val ads: List<Ad> = emptyList(),
        val error: Throwable? = null,
        val user: User? = null
)