package com.ekovpn.android.remote.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface AWSIPApiService {

    @GET("/")
    suspend fun fetchIP(): Response<ResponseBody>

}
