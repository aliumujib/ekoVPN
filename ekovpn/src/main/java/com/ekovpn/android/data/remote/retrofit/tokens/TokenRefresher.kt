package com.ekovpn.android.data.remote.retrofit.tokens

import android.util.Log
import com.ekovpn.android.BuildConfig
import com.ekovpn.android.data.remote.retrofit.APIServiceFactory
import com.google.gson.Gson
import javax.inject.Inject

class TokenRefresher @Inject constructor(
) {

    fun refreshToken(email: String, password: String): String? {
        val hashMap = HashMap<String, Any>()
        hashMap["email"] = email
        hashMap["password"] = password
        val data = APIServiceFactory.simpleEkoVPNApiService(
            BuildConfig.EKO_VPN_BASE_URL, Gson()
        ).syncAppLogin(hashMap)
        Log.d(TokenRefresher::class.java.simpleName, data.toString())
        return data.token
    }

}