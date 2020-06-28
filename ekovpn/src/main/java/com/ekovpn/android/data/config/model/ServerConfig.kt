/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.model

data class ServerConfig(
        val location: Location,
        val configfileurl: String = "N/A",
        val protocol: String
)

data class Location(
    val city: String,
    val country: String
)