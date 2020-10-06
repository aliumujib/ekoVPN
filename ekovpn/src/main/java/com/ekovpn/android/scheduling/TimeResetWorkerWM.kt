/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.scheduling

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ekovpn.android.data.cache.settings.SettingsPrefManager
import com.ekovpn.android.data.cache.settings.UserPrefManager

class TimeResetWorkerWM(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

    private val userSettingsPrefManager = SettingsPrefManager(appContext)
    private val userPrefManager: UserPrefManager = UserPrefManager(appContext)


    override fun doWork(): Result {

        Log.d(TAG, "RUNNING RESET TASK")
        if(userPrefManager.hasScheduledReseter()){
            userPrefManager.setTimeLeft(0)
            userSettingsPrefManager.setRemainingAdAllowance(43200001L)
        }

        userPrefManager.setHasScheduledReseter(true)
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    companion object {
        const val TAG = "TimeResetWorker"
    }

}