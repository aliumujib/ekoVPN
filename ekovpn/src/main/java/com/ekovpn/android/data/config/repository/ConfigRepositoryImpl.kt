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
import com.ekovpn.android.data.config.model.Profile
import com.ekovpn.android.data.config.model.Protocol
import com.ekovpn.android.data.config.model.ServerConfig
import com.google.gson.Gson
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
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
        val listOfFlows: List<Flow<Result<ServerConfig>>> = serverConfig.map {
            fileDownloader.downloadConfigFile(it)
        }

        return listOfFlows.merge()
                .takeWhile {
                    !it.getOrNull()?.location?.city.equals("END", true)
                }
                .flatMapConcat {
                    if (it.isSuccess) {
                        configureProfileForServer(it.getOrNull()!!)
                    } else {
                        flowOf(Result.failure(Exception("An error occurred for config ${it.getOrNull()}")))
                    }
                }.map { profile ->
                    if (profile.getOrNull() is Profile) {
                        userActionSaveProfile(profile.getOrNull() as Profile)
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("An error occurred for config"))
                    }
                }.onCompletion {
                    settingsPrefManager.setHasCompletedSetup()
                }
    }

    private fun userActionSaveProfile(result: Profile) {
        val profile = result.vpnProfile
        profile.mName = "${result.location.city}-${result.location.country}-${result.protocol.value}"
        profileManager.addProfile(profile)
        profileManager.saveProfile(context, profile)
        profileManager.saveProfileList(context)
    }

    private fun configureProfileForServer(serverConfig: ServerConfig): Flow<Result<Profile>> {
        return profileImporter.importServerConfig("file://${serverConfig.configfileurl}".toUri()).map {
            if (it.isSuccess) {
                File(serverConfig.configfileurl).delete()
                Result.success(Profile(it.getOrNull()!!, serverConfig.location, Protocol.fromString(serverConfig.protocol)))
            } else {
                Result.failure(it.exceptionOrNull() ?: Exception("An error occurred"))
            }
        }
    }

    companion object {
        const val FILE_NAME = "servers.json"
    }

}