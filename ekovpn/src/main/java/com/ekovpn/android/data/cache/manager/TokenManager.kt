package com.ekovpn.android.data.cache.manager

import android.content.Context
import com.ekovpn.android.data.cache.sharedprefs.CoreSharedPrefManager
import javax.inject.Inject


class TokenManager @Inject constructor(context: Context) : CoreSharedPrefManager(context) {

    fun clearToken() {
        delete(KEY_TOKEN)
    }

    fun saveToken(token: String?) {
        savePref(KEY_TOKEN, token)
    }

    fun getToken(): String? {
        return getPref(KEY_TOKEN)
    }


    companion object {
        const val KEY_TOKEN = "key_token"
    }

}