package com.ekovpn.android.data.remote.retrofit

import com.ekovpn.android.data.remote.models.IPResolve
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IPStackApiService {

    @GET("{ip}")
    suspend fun resolveIpToLocation( @Path("ip") id: String, @Query("access_key") apiKey: String): IPResolve

}
