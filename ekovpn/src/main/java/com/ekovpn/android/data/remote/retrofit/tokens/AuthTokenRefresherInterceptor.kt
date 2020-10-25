package com.ekovpn.android.data.remote.retrofit.tokens

import android.util.Log
import com.ekovpn.android.BuildConfig
import com.ekovpn.android.data.cache.manager.TokenManager
import com.ekovpn.android.data.remote.models.EkoVPNAPIResponse
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class AuthTokenRefresherInterceptor(
        private val tokenManager: TokenManager,
        private val tokenRefresher: TokenRefresher
) : Authenticator {

    private var countOfRetry: Int = 0

    data class Error(
            val message: String,
            val success: Boolean
    )

    private fun getErrorMessage(responseBody: ResponseBody?): String? {
        return try {
            val gson = Gson()
            var apiResponse = gson.fromJson(responseBody?.string(), Error::class.java)
            apiResponse.message.let {
                println("ERROR IS: $it")
            }
            apiResponse.message
        } catch (e: Exception) {
            e.printStackTrace()
            "There was an error handling your request, please retry."
        }
    }

    private fun getErrorMessageIfExists(response: Response?): String? {
        var errorMessage: String? = null
        try {
            errorMessage = getErrorMessage(response?.body())
        } catch (ex: Exception) {
            Log.d(AuthTokenRefresherInterceptor::class.java.simpleName, "Failed re auth token", ex)
        }
        return errorMessage
    }

    companion object {
        private val TAG = AuthTokenRefresherInterceptor::class.java.canonicalName
    }

    override fun authenticate(route: Route?, response: Response): Request? {

        var errorMessage: String? = getErrorMessageIfExists(response)
        if (response.code() == 401 && countOfRetry < 4 && errorMessage?.contains("jwt expired", true) == true) {
            // We need to have a token in order to refresh it.
            Log.d(AuthTokenRefresherInterceptor::class.java.simpleName, "ReAuthenticating token")

            val login = BuildConfig.ANDROID_APP_LOGIN
            val password = BuildConfig.ANDROID_APP_PASSWORD

            val newToken = tokenRefresher.refreshToken(login, password)
            tokenManager.saveToken(newToken)

            countOfRetry++

            Log.d(
                    AuthTokenRefresherInterceptor::class.java.simpleName,
                    "Re authenticating token success on retry $countOfRetry"
            )
            val request = response.request().newBuilder()
                    .header("Authorization", "Bearer " + tokenManager.getToken())
                    .build()

            return request

        } else {
            countOfRetry = 0
            Log.d(AuthTokenRefresherInterceptor::class.java.simpleName, "No need to refresh token")
            return null
        }
    }
}