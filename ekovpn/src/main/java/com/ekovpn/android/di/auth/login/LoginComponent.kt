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
package com.ekovpn.android.di.auth.login



import com.ekovpn.android.di.auth.AuthComponent
import com.ekovpn.android.di.auth.splash.SplashComponent
import com.ekovpn.android.di.main.VPNComponent
import com.ekovpn.android.di.scopes.FragmentScope
import com.ekovpn.android.view.auth.login.LoginFragment
import com.ekovpn.android.view.auth.splashscreen.SplashFragment
import com.ekovpn.android.view.auth.success.SuccessFragment
import com.ekovpn.android.view.main.home.HomeFragment
import dagger.Component

/**
 * Class for which a fully-formed, dependency-injected implementation is to
 * be generated from [LoginModule].
 *
 * @see Component
 */
@FragmentScope
@Component(
    modules = [LoginModule::class],
    dependencies = [AuthComponent::class])
interface LoginComponent {

    fun inject(fragment: LoginFragment)

}
