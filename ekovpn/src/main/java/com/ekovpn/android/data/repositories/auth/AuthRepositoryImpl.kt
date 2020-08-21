/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.auth

import android.content.Context
import com.ekovpn.android.BuildConfig
import com.ekovpn.android.data.cache.manager.TokenManager
import com.ekovpn.android.data.cache.room.dao.UsersDao
import com.ekovpn.android.data.remote.retrofit.EkoVPNApiService
import com.ekovpn.android.models.User
import com.ekovpn.android.utils.ext.getDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val userDao: UsersDao,
                                             private val context: Context,
                                             private val tokenManager: TokenManager,
                                             private val ekoVPNAPIService: EkoVPNApiService) : AuthRepository {


    override fun createAccount(): Flow<User> {
        return flow {
            userDao.deleteAll()
            val mapOfArgs = mapOf("imei" to context.getDeviceId()!!)
            val user = ekoVPNAPIService.createNewUser(mapOfArgs)
            user.data?.toUserCacheModel()?.let {
                userDao.insert(it)
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
            userDao.deleteAll()
            val user = ekoVPNAPIService.fetchExistingUser(accountNumber, context.getDeviceId()!!)
            user.data?.toUserCacheModel()?.let {
                userDao.insert(it)
            }
            emit(userDao.getUser()?.toUser()!!)
        }.flowOn(Dispatchers.IO)
    }

    override fun fetchUserByOrderNumber(orderNumber: String): Flow<User> {
        return flow {
            userDao.deleteAll()
            val mapOfArgs = mapOf("imei" to context.getDeviceId()!!)
            val user = ekoVPNAPIService.fetchExistingUserByOrderNumber(orderNumber)
            user.data?.toUserCacheModel()?.let {
                userDao.insert(it)
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

    override fun redeemReferral(referralCode: String): Flow<User> {
        return flow {
            val oldUser = userDao.getUser()!!.id
            val mapOfArgs = mapOf("referred_by" to referralCode)
            val user = ekoVPNAPIService.updateUserByReferralId(oldUser, mapOfArgs)
            userDao.deleteAll()
            user.data?.toUserCacheModel()?.let {
                userDao.insert(it)
            }
            emit(userDao.getUser()?.toUser()!!)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return userDao.getUser() != null
    }
}