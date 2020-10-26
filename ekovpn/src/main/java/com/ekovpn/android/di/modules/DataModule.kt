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
package com.ekovpn.android.di.modules

import com.ekovpn.android.data.repositories.ads.AdsRepository
import com.ekovpn.android.data.repositories.ads.AdsRepositoryImpl
import com.ekovpn.android.data.repositories.analytics.AnalyticsRepository
import com.ekovpn.android.data.repositories.analytics.AnalyticsRepositoryImpl
import com.ekovpn.android.data.repositories.auth.AuthRepository
import com.ekovpn.android.data.repositories.auth.AuthRepositoryImpl
import com.ekovpn.android.data.repositories.config.repository.ConfigRepository
import com.ekovpn.android.data.repositories.config.repository.ConfigRepositoryImpl
import com.ekovpn.android.data.repositories.servers.ServersRepository
import com.ekovpn.android.data.repositories.servers.ServersRepositoryImpl
import com.ekovpn.android.data.repositories.settings.SettingsRepository
import com.ekovpn.android.data.repositories.settings.SettingsRepositoryImpl
import com.ekovpn.android.data.repositories.user.UserRepository
import com.ekovpn.android.data.repositories.user.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Singleton
    @Provides
    fun providesConfigRepository(configRepositoryImpl: ConfigRepositoryImpl): ConfigRepository {
        return configRepositoryImpl
    }

    @Singleton
    @Provides
    fun providesServersRepository(serversRepositoryImpl: ServersRepositoryImpl): ServersRepository {
        return serversRepositoryImpl
    }

    @Singleton
    @Provides
    fun providesSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository {
        return settingsRepositoryImpl
    }

    @Singleton
    @Provides
    fun providesUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository {
        return userRepositoryImpl
    }

    @Singleton
    @Provides
    fun providesAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository {
        return authRepositoryImpl
    }

    @Singleton
    @Provides
    fun providesAdsRepository(adsRepository: AdsRepositoryImpl): AdsRepository {
        return adsRepository
    }

    @Singleton
    @Provides
    fun providesAnalyticsRepository(analyticsRepository: AnalyticsRepositoryImpl): AnalyticsRepository {
        return analyticsRepository
    }
}
