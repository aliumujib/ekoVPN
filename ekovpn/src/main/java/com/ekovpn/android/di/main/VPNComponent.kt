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
package com.ekovpn.android.di.main


import android.content.Context
import com.ekovpn.android.data.ads.AdsRepository
import com.ekovpn.android.data.repositories.servers.ServersRepository
import com.ekovpn.android.data.repositories.settings.SettingsRepository
import com.ekovpn.android.data.user.UserRepository
import com.ekovpn.android.di.components.CoreComponent
import com.ekovpn.android.di.scopes.ActivityScope
import com.ekovpn.android.view.main.VpnActivity
import dagger.Component

/**
 * Class for which a fully-formed, dependency-injected implementation is to
 * be generated from [VPNModule].
 *
 * @see Component
 */
@ActivityScope
@Component(
    modules = [VPNModule::class],
    dependencies = [CoreComponent::class])
interface VPNComponent {

    fun context(): Context

    fun settingsRepository(): SettingsRepository

    fun serversRepository(): ServersRepository

    fun userRepository (): UserRepository

    fun adsRepository (): AdsRepository

    fun inject(vpnActivity: VpnActivity)

}
