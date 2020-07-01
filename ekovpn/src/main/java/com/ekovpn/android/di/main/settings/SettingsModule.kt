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
package com.ekovpn.android.di.main.settings

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import com.ekovpn.android.data.settings.SettingsRepository
import com.ekovpn.android.di.scopes.FragmentScope
import com.ekovpn.android.utils.ext.viewModel
import com.ekovpn.android.view.main.settings.SettingsFragment
import com.ekovpn.android.view.main.settings.SettingsViewModel
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Class that contributes to the object graph [SettingsComponent].
 *
 * @see Module
 */
@ExperimentalCoroutinesApi
@Module
class SettingsModule(@VisibleForTesting(otherwise = PRIVATE) val fragment: SettingsFragment) {


    @FragmentScope
    @Provides
    fun providesViewModel(settingsRepository: SettingsRepository) = fragment.viewModel {
        SettingsViewModel(settingsRepository)
    }

}