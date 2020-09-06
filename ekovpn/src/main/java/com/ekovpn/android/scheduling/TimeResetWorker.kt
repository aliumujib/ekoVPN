/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.scheduling

import com.ekovpn.android.data.cache.settings.SettingsPrefManager
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit


class TimeResetWorker : DailyJob() {
    private val userSettingsPrefManager = SettingsPrefManager(context)

    override fun onRunDailyJob(p0: Params): DailyJobResult {
        userSettingsPrefManager.setRemainingAdAllowance(43200001L)
        return DailyJobResult.SUCCESS;
    }

    companion object{
        const val TAG = "TimeResetWorker"

        fun scheduleJob(): Int {
            return schedule(JobRequest.Builder(TAG), TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(2))
        }
    }

}