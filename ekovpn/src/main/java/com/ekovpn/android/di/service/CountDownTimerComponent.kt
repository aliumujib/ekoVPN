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
package com.ekovpn.android.di.service


import com.ekovpn.android.data.user.UserRepository
import com.ekovpn.android.di.components.CoreComponent
import com.ekovpn.android.di.scopes.ActivityScope
import com.ekovpn.android.service.EkoVPNMgrService
import dagger.Component

/**
 * Class for which a fully-formed, dependency-injected implementation is to
 * be generated from [CountDownTimerModule].
 *
 * @see Component
 */
@ActivityScope
@Component(
    modules = [CountDownTimerModule::class],
    dependencies = [CoreComponent::class])
interface CountDownTimerComponent {

    fun userRepository (): UserRepository

    fun inject(ekoVPNMgrService: EkoVPNMgrService)

}
