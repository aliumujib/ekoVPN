/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.utils.ext

import kotlinx.coroutines.flow.*

inline fun <reified K, V> buildMap(keys: Set<K>, crossinline valueFunc: (K) -> Flow<V>): Flow<Map<K, V>> = flow {
    val keysSize = keys.size
    val valuesMap = HashMap<K, V>(keys.size)
    flowOf(*keys.toTypedArray())
            .flatMapMerge { key -> valueFunc(key).map {v -> Pair(key, v)} }
            .collect { (key, value) ->
                valuesMap[key] = value
                if (valuesMap.keys.size == keysSize) {
                    emit(valuesMap.toMap())
                }
            }
}