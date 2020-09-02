/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.user

import android.content.Context
import com.ekovpn.android.data.cache.room.dao.UsersDao
import com.ekovpn.android.data.cache.room.entities.UserCacheModel
import com.ekovpn.android.data.cache.settings.UserPrefManager
import com.ekovpn.android.data.remote.models.auth.RemoteDevice
import com.ekovpn.android.data.remote.models.auth.RemoteUser
import com.ekovpn.android.data.remote.retrofit.EkoVPNApiService
import com.ekovpn.android.data.repositories.auth.AuthRepository
import com.ekovpn.android.models.Device
import com.ekovpn.android.models.User
import com.ekovpn.android.utils.ext.getDeviceId
import com.ekovpn.android.utils.ext.getModelName
import com.ekovpn.android.utils.ext.handleHttpErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val userPrefManager: UserPrefManager,
                                             private val context: Context,
                                             private val authRepository: AuthRepository,
                                             private val ekoVPNAPIService: EkoVPNApiService,
                                             private val userDao: UsersDao) : UserRepository {

    override fun getTimeLeft(): Long {
        return userPrefManager.getTimeLeft()
    }

    override fun setTimeLeft(timeLeft: Long) {
        userPrefManager.setTimeLeft(timeLeft)
    }

    override fun addToTimeLeft(newTime: Long) {
        val original = getTimeLeft()
        val new = original + newTime
        setTimeLeft(new)
    }

    override fun streamCurrentUser(): Flow<User> {
        return userDao.streamUser()
                .filter {
                    it != null
                }
                .map {
                    it.toUser()
                }.flowOn(Dispatchers.IO)
    }

    override fun isSignedIn():Boolean{
        return userPrefManager.getUserId() != null
    }

    override fun redeemReferral(referralCode: String): Flow<User> {
        return flow {
            val oldUser = userDao.getUser()!!.id
            val mapOfArgs = mapOf("referred_by" to referralCode)
            val user = ekoVPNAPIService.updateUserByReferralId(oldUser, mapOfArgs)
            user.data?.toUserCacheModel()?.let {
                saveCurrentUser(it)
            }
            addToTimeLeft(3600000L)
            emit(userDao.getUser()?.toUser()!!)
        }.flowOn(Dispatchers.IO)
                .handleHttpErrors()
    }

    override fun refreshCurrentUser(): Flow<Unit> {
        return flow{
            emit(userPrefManager.getUserId()!!)
        }.flatMapConcat {
            authRepository.fetchUserByAccountNumber(it)
        }.take(1).flatMapConcat {
            claimReferralRewards()
        }.flowOn(Dispatchers.IO)
                .handleHttpErrors()
    }

    override fun claimReferralRewards(): Flow<Unit> {
        return flow {
            val oldUser = userDao.getUser()
            val count = ekoVPNAPIService.claimUserReferrals(oldUser?.account_id!!).data
            addToTimeLeft(3600000L * count!!)
            emit(Unit)
        }.handleHttpErrors()
                .flowOn(Dispatchers.IO)
    }

    override fun deleteDevice(device: Device): Flow<User> {
        return flow {
            val oldUser = userDao.getUser()
            val deviceRemote = RemoteDevice(device.imei, device.device)
            val user = ekoVPNAPIService.deleteDeviceFromUserIMEI(oldUser?.account_id!!, deviceRemote.toJSONString())
            user.data?.toUserCacheModel()?.let {
                saveCurrentUser(it)
            }
            emit(userDao.getUser()?.toUser()!!)
        }.flowOn(Dispatchers.IO)
                .handleHttpErrors()
    }

    private suspend fun saveCurrentUser(it: UserCacheModel) {
        userDao.deleteAll()
        userDao.insert(it)
        userPrefManager.setUserAccountId(it.account_id)
    }

    override fun updateUserWithOrderId(orderId: String): Flow<User> {
        return flow {
            val userId = userDao.getUser()!!.id
            val map = mutableMapOf<String, String>()
            map["order_number"] = orderId
            map["account_type"] = "paid"
            val user = ekoVPNAPIService.updateUserAccount(userId, map).data
            saveCurrentUser(user!!.toUserCacheModel())
            emit(user.toUser())
        }.flowOn(Dispatchers.IO)
    }

}