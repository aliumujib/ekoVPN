/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.repository

import com.ekovpn.android.data.config.ServerConfig
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {

    fun fetchAndConfigureServers(): Flow<Result<Unit>>

    fun hasConfiguredServers(): Boolean
    fun configureOVPNServers(serverConfig: Array<ServerConfig>): Flow<Result<Unit>>
    fun configureIkeV2Servers(serverConfig: Array<ServerConfig>): Flow<Result<Unit>>
}