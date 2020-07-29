/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.remote.models

data class IPResolve(
        val city: String,
        val continent_code: String,
        val continent_name: String,
        val country_code: String,
        val country_name: String,
        val ip: String,
        val latitude: Double,
        val location: Location,
        val longitude: Double,
        val region_code: String,
        val region_name: String,
        val type: String,
        val zip: String
)

data class Location(
    val calling_code: String,
    val capital: String,
    val country_flag: String,
    val country_flag_emoji: String,
    val country_flag_emoji_unicode: String,
    val geoname_id: Int,
    val is_eu: Boolean,
    val languages: List<Language>
)

data class Language(
    val code: String,
    val name: String,
    val native: String
)