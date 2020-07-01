/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.ekovpn.android.cache.settings.SettingsPrefManager
import com.ekovpn.android.data.config.downloader.FileDownloader
import com.ekovpn.android.data.config.importer.OVPNProfileImporter
import com.ekovpn.android.data.config.model.VPNServer
import com.ekovpn.android.data.config.model.Protocol
import com.ekovpn.android.data.config.model.ServerConfig
import com.ekovpn.android.data.config.model.ServerSetUp
import com.google.gson.Gson
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject


class ConfigRepositoryImpl @Inject constructor(private val context: Context,
                                               private val fileDownloader: FileDownloader,
                                               private val profileManager: ProfileManager,
                                               private val profileImporter: OVPNProfileImporter,
                                               private val settingsPrefManager: SettingsPrefManager) : ConfigRepository {

    private fun loadFromJson() = context.assets.open(FILE_NAME).bufferedReader().use {
        it.readText()
    }

    override fun hasConfiguredServers(): Boolean {
        return settingsPrefManager.getHasCompletedSetup()
    }

    override fun fetchAndConfigureServers(): Flow<Result<Unit>> {
        val data = loadFromJson()
        val gson = Gson()
        val serverConfig = gson.fromJson(data, Array<ServerConfig>::class.java)
        serverConfig.forEach {
            Log.d(ConfigRepositoryImpl::class.java.simpleName, it.location.toString())
        }

        serverConfig.sortBy { it.location.city }
        val ovpnConfigs: MutableList<Flow<Result<ServerSetUp>>> = mutableListOf()

        serverConfig.forEach { server ->
            server.open_vpn.forEach {
                ovpnConfigs.add(fileDownloader.downloadConfigFile(server.location, Protocol.fromString(it.protocol), it.configfileurl))
            }
        }

        return ovpnConfigs.merge()
                .takeWhile {
                    !it.getOrNull()?.location_?.city.equals("ZZ", true)
                }.filterNot {
                    it.getOrNull()?.location_?.city.equals("ZZ", true)
                }
                .flatMapConcat {
                    if (it.isSuccess) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Configuring: ${it.getOrNull()}")
                        configureOVPNProfileForServer(it.getOrNull() as ServerSetUp.OVPNSetup)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed to configure: ${it.getOrNull()}")
                        flowOf(Result.failure(Exception("An error occurred for config ${it.getOrNull()}")))
                    }
                }.map { profile ->
                    if (profile.getOrNull() is VPNServer) {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saving: ${profile.getOrNull()}")
                        userActionSaveProfile(profile.getOrNull() as VPNServer.OVPNServer)
                        Result.success(Unit)
                    } else {
                        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed Saving: ${profile.getOrNull()}")
                        Result.failure(Exception("An error occurred for config"))
                    }
                }.onCompletion {
                    settingsPrefManager.setHasCompletedSetup()
                }
    }

    private fun userActionSaveProfile(result: VPNServer.OVPNServer) {
        val profile = result.openVpnProfile
        profile.mName = "${result.location.city}-${result.location.country}-${result.protocol.value}"
        profileManager.addProfile(profile)
        profileManager.saveProfile(context, profile)
        profileManager.saveProfileList(context)
        Log.d(ConfigRepositoryImpl::class.java.simpleName, "Saved: $result")
    }

    private fun configureOVPNProfileForServer(serverSetUp: ServerSetUp.OVPNSetup): Flow<Result<VPNServer>> {
        return profileImporter.importServerConfig("file://${serverSetUp.ovpnFileDir}".toUri()).map {
            if (it.isSuccess) {
                Log.d(ConfigRepositoryImpl::class.java.simpleName, "Importing: ${it.getOrNull()}")
                File(serverSetUp.ovpnFileDir).delete()
                Result.success(VPNServer.OVPNServer(it.getOrNull()!!, serverSetUp.location, serverSetUp.protocol))
            } else {
                Log.d(ConfigRepositoryImpl::class.java.simpleName, "Failed Importing: ${it.getOrNull()}")
                Result.failure(it.exceptionOrNull() ?: Exception("An error occurred"))
            }
        }
    }

    companion object {
        const val FILE_NAME = "servers.json"
    }

}