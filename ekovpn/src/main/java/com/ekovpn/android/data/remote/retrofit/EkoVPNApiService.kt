package com.ekovpn.android.data.remote.retrofit

import com.ekovpn.android.data.repositories.config.ServerConfig
import com.ekovpn.android.data.remote.models.EkoVPNAPIAuthResponse
import com.ekovpn.android.data.remote.models.EkoVPNAPIResponse
import com.ekovpn.android.data.remote.models.ads.RemoteAd
import com.ekovpn.android.data.remote.models.auth.RemoteApp
import com.ekovpn.android.data.remote.models.auth.RemoteUser
import retrofit2.Call
import retrofit2.http.*

interface EkoVPNApiService {

    @POST("authenticate")
    @FormUrlEncoded
    fun syncAppLogin(@FieldMap query: Map<String, @JvmSuppressWildcards Any>): Call<EkoVPNAPIAuthResponse<RemoteApp>>

    @POST("authenticate")
    @FormUrlEncoded
    suspend fun appLogin(@FieldMap query: Map<String, @JvmSuppressWildcards Any>): EkoVPNAPIAuthResponse<RemoteApp>

    @POST("user")
    @FormUrlEncoded
    suspend fun createNewUser(@FieldMap query: Map<String, @JvmSuppressWildcards Any>): EkoVPNAPIResponse<RemoteUser>

    @GET("user/account/{userAccount}/imei/{imei}")
    suspend fun fetchExistingUser(@Path("userAccount") userAccount: String,@Path("imei")  imei: String): EkoVPNAPIResponse<RemoteUser>

    @DELETE("user/account/{userAccount}/imei/{imei}")
    suspend fun deleteDeviceFromUserIMEI(@Path("userAccount") userAccount: String,@Path("imei")  imei: String): EkoVPNAPIResponse<RemoteUser>

    @GET("user/order/{orderNumber}")
    suspend fun fetchExistingUserByOrderNumber(@Path("orderNumber") orderNumber: String, @QueryMap query: Map<String, @JvmSuppressWildcards Any>): EkoVPNAPIResponse<RemoteUser>

    @PUT("user/id/{userId}")
    @FormUrlEncoded
    suspend fun updateUserAccount(@Path("userId") userAccount: String, @FieldMap query: Map<String, @JvmSuppressWildcards Any>): EkoVPNAPIResponse<RemoteUser>

    @PUT("user/add-referral/{userId}")
    @FormUrlEncoded
    suspend fun updateUserByReferralId(@Path("userId") userAccount: String, @FieldMap query: Map<String, @JvmSuppressWildcards Any>): EkoVPNAPIResponse<RemoteUser>

    @GET("user/claim-referral/{userId}")
    suspend fun claimUserReferrals(@Path("userId") userAccount: String): EkoVPNAPIResponse<Int>

    @GET("json/ads")
    suspend fun fetchAdsData(): EkoVPNAPIResponse<List<RemoteAd>>

    @GET("json/setup")
    suspend fun fetchServerConfig(): EkoVPNAPIResponse<List<ServerConfig>>

}
