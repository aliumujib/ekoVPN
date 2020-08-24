package com.ekovpn.android.data.remote.retrofit

import android.net.TrafficStats
import com.ekovpn.android.data.cache.manager.TokenManager
import com.ekovpn.android.data.remote.retrofit.tokens.AuthInterceptor
import com.ekovpn.android.data.remote.retrofit.tokens.AuthTokenRefresherInterceptor
import com.ekovpn.android.data.remote.retrofit.tokens.TokenRefresher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.Socket


object APIServiceFactory {

    fun iPStackAPIService(apiURL: String, gson: Gson = makeGson()): IPStackApiService {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(logging)
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS)

        val retrofit = Retrofit.Builder()
                .baseUrl(apiURL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        return retrofit.create(IPStackApiService::class.java)
    }

    fun simpleEkoVPNApiService(apiURL: String, gson: Gson = makeGson()): EkoVPNApiService {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(logging)
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS)

        val retrofit = Retrofit.Builder()
                .baseUrl(apiURL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        return retrofit.create(EkoVPNApiService::class.java)
    }

    fun ekoVPNApiService(apiURL: String, gson: Gson = makeGson(), authInterceptor: AuthInterceptor, tokenManager: TokenManager, tokenRefresher: TokenRefresher): EkoVPNApiService {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClientBuilder = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .authenticator(AuthTokenRefresherInterceptor(
                        tokenManager,
                        tokenRefresher))
        httpClientBuilder.addInterceptor(logging)

        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS)

        val retrofit = Retrofit.Builder()
                .baseUrl(apiURL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        return retrofit.create(EkoVPNApiService::class.java)
    }

    fun amazonWSAPIService(apiURL: String, gson: Gson = makeGson()): AWSIPApiService {
        val logging = HttpLoggingInterceptor()
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(logging)
        logging.level = HttpLoggingInterceptor.Level.BODY
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS)

        val retrofit = Retrofit.Builder()
                .baseUrl(apiURL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        return retrofit.create(AWSIPApiService::class.java)
    }


    private fun makeGson(): Gson {
        return GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()
    }

}