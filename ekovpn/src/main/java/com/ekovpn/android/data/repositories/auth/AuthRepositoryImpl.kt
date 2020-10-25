/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.auth

import android.content.Context
import com.ekovpn.android.BuildConfig
import com.ekovpn.android.data.cache.manager.TokenManager
import com.ekovpn.android.data.cache.room.dao.UsersDao
import com.ekovpn.android.data.cache.room.entities.UserCacheModel
import com.ekovpn.android.data.cache.settings.UserPrefManager
import com.ekovpn.android.data.remote.models.auth.RemoteDevice
import com.ekovpn.android.data.remote.retrofit.EkoVPNApiService
import com.ekovpn.android.models.User
import com.ekovpn.android.utils.ext.getDeviceId
import com.ekovpn.android.utils.ext.getModelName
import com.ekovpn.android.utils.ext.handleHttpErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val userDao: UsersDao,
                                             private val context: Context,
                                             private val userPrefManager: UserPrefManager,
                                             private val tokenManager: TokenManager,
                                             private val ekoVPNAPIService: EkoVPNApiService) : AuthRepository {

    private suspend fun saveCurrentUser(it: UserCacheModel) {
        userDao.deleteAll()
        userDao.insert(it)
        userPrefManager.setUserAccountId(it.account_id)
    }

    override fun createAccount(): Flow<User> {
        return flow {
            val device = RemoteDevice(context.getDeviceId(), getModelName())
            val mapOfArgs = mapOf("imei" to device.toJSONString())
            val user = ekoVPNAPIService.createNewUser(mapOfArgs)
            user.data?.toUserCacheModel()?.let {
                saveCurrentUser(it)
            }
            emit(userDao.getUser()?.toUser()!!)
        }.flowOn(Dispatchers.IO)
    }

    override fun loginToApp(): Flow<String> {
        return flow {
            emit(login())
        }
    }

    override fun fetchUserByAccountNumber(accountNumber: String): Flow<User> {
        return flow {
            val device = RemoteDevice(context.getDeviceId(), getModelName())
            val user = ekoVPNAPIService.fetchExistingUser(accountNumber, device.toJSONString())
            user.data?.toUserCacheModel()?.let {
                saveCurrentUser(it)
            }
            emit(userDao.getUser()?.toUser()!!)
        }.flowOn(Dispatchers.IO)
                .handleHttpErrors()
    }

    override fun fetchUserByOrderNumber(orderNumber: String): Flow<User> {
        return flow {
            val device = RemoteDevice(context.getDeviceId(), getModelName())
            val mapOfArgs = mapOf("imei" to device.toJSONString())
            val user = ekoVPNAPIService.fetchExistingUserByOrderNumber(orderNumber, mapOfArgs)
            user.data?.toUserCacheModel()?.let {
                saveCurrentUser(it)
            }
            emit(userDao.getUser()?.toUser()!!)
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun login(): String {
        val mapOfArgs = mapOf("appId" to BuildConfig.ANDROID_APP_LOGIN, "appSecret" to BuildConfig.ANDROID_APP_PASSWORD)
        val app = ekoVPNAPIService.appLogin(mapOfArgs)
        app.token?.let {
            tokenManager.saveToken(it)
        }
        return app.token!!
    }



    override suspend fun isUserLoggedIn(): Boolean {
        return userDao.getUser() != null
    }

}