package com.ekovpn.android.remote.retrofit

import com.ekovpn.android.remote.models.IPResolve
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IPStackApiService {

    @GET("{ip}")
    suspend fun resolveIpToLocation( @Path("ip") id: String, @Query("access_key") apiKey: String): IPResolve

}
