/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.ads

import android.content.Context
import android.util.Log
import com.ekovpn.android.models.Ad
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdsRepositoryImpl @Inject constructor(val context: Context) : AdsRepository {

    private fun loadJSONData(): Array<AdModel> {
        val data = loadFromJson()
        return Gson().fromJson(data, Array<AdModel>::class.java)
    }

    override fun fetchAds(): Flow<List<Ad>> {
        return flowOf(loadJSONData()).map { array ->
            array.map {
                Ad.fromAdModel(it)
            }
        }
    }

    private fun loadFromJson() = context.assets.open(FILE_NAME).bufferedReader().use {
        it.readText()
    }

    companion object {
        const val FILE_NAME = "ad_types.json"
    }

}