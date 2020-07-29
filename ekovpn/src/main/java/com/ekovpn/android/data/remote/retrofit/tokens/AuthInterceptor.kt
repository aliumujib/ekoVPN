package com.ekovpn.android.data.remote.retrofit.tokens

import android.util.Log
import com.ekovpn.android.data.cache.manager.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/***
 * **/

class AuthInterceptor @Inject constructor(private var tokenManager: TokenManager) : Interceptor {


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {

        val request = chain.request()

        val requestBuilder = request.newBuilder()

        if (tokenManager.getToken() != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + tokenManager.getToken())
        }

        var response: Response? = null

        try {

            response = chain.proceed(requestBuilder.build())

        } catch (e: Exception) {
            Log.d(TAG, "<-- HTTP FAILED: $e")
            throw e
        }

        return response
    }

    companion object {
        private val TAG = AuthInterceptor::class.java.canonicalName
    }
}