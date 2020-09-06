/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.scheduling

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator


class TimeResetWorkerCreator : JobCreator {

    @Nullable
    override fun create(@NonNull tag: String): Job? {
        return when (tag) {
            TimeResetWorker.TAG -> TimeResetWorker()
            else -> throw IllegalArgumentException("No appropriate worker for $tag")
        }
    }

}