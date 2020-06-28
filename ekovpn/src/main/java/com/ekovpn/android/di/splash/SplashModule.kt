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
package com.ekovpn.android.di.splash

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import com.ekovpn.android.data.config.repository.ConfigRepository
import com.ekovpn.android.di.scopes.ActivityScope
import com.ekovpn.android.utils.ext.viewModel
import com.ekovpn.android.view.SplashActivity
import com.ekovpn.android.view.SplashViewModel
import dagger.Module
import dagger.Provides

/**
 * Class that contributes to the object graph [SplashComponent].
 *
 * @see Module
 */

@Module
class SplashModule(
        @VisibleForTesting(otherwise = PRIVATE)
        val activity: SplashActivity
) {


    @ActivityScope
    @Provides
    fun providesViewModel(
            configRepository: ConfigRepository
    ) = activity.viewModel {
        SplashViewModel(
                configRepository
        )
    }

}
