/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.remote.models

data class EkoVPNAPIResponse<T>(val success: Boolean, val message: String, val data: T?)

data class EkoVPNAPIAuthResponse<T>(val success: Boolean, val message: String, val data: T?, val token: String?)