package com.ekovpn.android.data.remote.retrofit

import com.ekovpn.android.data.config.ServerConfig
import com.ekovpn.android.data.remote.models.EkoVPNAPIAuthResponse
import com.ekovpn.android.data.remote.models.EkoVPNAPIResponse
import com.ekovpn.android.data.remote.models.ads.RemoteAd
import com.ekovpn.android.data.remote.models.auth.RemoteApp
import com.ekovpn.android.data.remote.models.auth.RemoteUser
import retrofit2.http.*

interface EkoVPNApiService {

    @POST("authenticate")
    suspend fun appLogin(@FieldMap query: Map<String, @JvmSuppressWildcards Any>): EkoVPNAPIAuthResponse<RemoteApp>

    @POST("user")
    suspend fun createNewUser(): EkoVPNAPIResponse<RemoteUser>

    @GET("user/account/{userAccount}")
    suspend fun fetchExistingUser(@Path("userAccount") userAccount: String): EkoVPNAPIResponse<RemoteUser>

    @PUT("user/account/{userId}")
    suspend fun updateUserAccount(@Path("userId") userAccount: String, @FieldMap query: Map<String, @JvmSuppressWildcards Any>): EkoVPNAPIResponse<RemoteUser>

    @GET("json/ads")
    suspend fun fetchAdsData(): EkoVPNAPIResponse<List<RemoteAd>>

    @GET("json/setup")
    suspend fun fetchServerConfig(): EkoVPNAPIResponse<List<ServerConfig>>

}
