/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.utils.ext

import android.util.Log
import com.ekovpn.android.data.remote.retrofit.tokens.AuthTokenRefresherInterceptor
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import retrofit2.HttpException

inline fun <reified K, V> runOnAll(keys: Set<K>, crossinline valueFunc: (K) -> Flow<V>): Flow<Map<K, V>> = flow {
    val keysSize = keys.size
    val valuesMap = HashMap<K, V>(keys.size)
    flowOf(*keys.toTypedArray())
            .flatMapMerge { key -> valueFunc(key).map { v -> Pair(key, v) } }
            .collect { (key, value) ->
                valuesMap[key] = value
                if (valuesMap.keys.size == keysSize) {
                    emit(valuesMap.toMap())
                }
            }
}


inline fun <T> Flow<T>.handleHttpErrors(): Flow<T> {
    return this.onCompletion { error ->
        try {
            if (error is HttpException && error.response()?.errorBody() != null) {
                val errorBody = error.response()!!.errorBody()
                val gson = Gson()
                val errorString = errorBody!!.string()
                Log.d("Error Handler Ext", errorBody!!.string())
                val apiResponse = gson.fromJson(errorString, AuthTokenRefresherInterceptor.Error::class.java)
                apiResponse.message.let {
                    println("Error Handler Ext: $it")
                }
                throw Throwable(apiResponse.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Throwable("There was an error handling your request, please retry.")
        }
    }
}