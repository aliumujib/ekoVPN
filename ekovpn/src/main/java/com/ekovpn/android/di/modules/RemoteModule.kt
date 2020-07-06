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

import com.ekovpn.android.BuildConfig
import com.ekovpn.android.remote.retrofit.APIServiceFactory
import com.ekovpn.android.remote.retrofit.AWSIPApiService
import com.ekovpn.android.remote.retrofit.IPStackApiService
import dagger.Module
import dagger.Provides

@Module
abstract class RemoteModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideIPStackAPIService(): IPStackApiService {
            return APIServiceFactory.iPStackAPIService(BuildConfig.IP_STACK_BASE_URL)
        }

        @Provides
        @JvmStatic
        fun provideAWSIPApiService(): AWSIPApiService {
            return APIServiceFactory.amazonWSAPIService(BuildConfig.AWS_IP_BASE_URL)
        }

    }

}
