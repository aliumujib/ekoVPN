/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.ads

import com.ekovpn.android.models.Ad
import kotlinx.coroutines.flow.Flow

interface AdsRepository {

    fun fetchAds(): Flow<List<Ad>>

}