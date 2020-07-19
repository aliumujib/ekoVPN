/*
 * Copyright 2020 Abdul-Mujeeb Aliu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ekovpn.android.di.components

import android.content.Context
import com.ekovpn.android.data.ads.AdsRepository
import com.ekovpn.android.data.config.repository.ConfigRepository
import com.ekovpn.android.data.servers.ServersRepository
import com.ekovpn.android.data.settings.SettingsRepository
import com.ekovpn.android.data.user.UserRepository
import com.ekovpn.android.di.modules.*
import com.ekovpn.android.di.scopes.AppScope
import com.ekovpn.android.utils.flow.PostExecutionThread
import dagger.Component
import javax.inject.Singleton

/**
 * Core component that all module's components depend on.
 *
 * @see Component
 */

@Singleton
@Component(modules = [
    ContextModule::class,
    RemoteModule::class,
    DataModule::class,
    CacheModule::class,
    UtilsModule::class
])
interface CoreComponent {

    fun context(): Context

    fun configRepository(): ConfigRepository

    fun serversRepository(): ServersRepository

    fun settingsRepository(): SettingsRepository

    fun userRepository(): UserRepository

    fun adsRepository (): AdsRepository

    fun postExecutionThread(): PostExecutionThread

}
