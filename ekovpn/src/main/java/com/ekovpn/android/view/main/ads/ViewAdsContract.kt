package com.ekovpn.android.view.main.ads

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.ekovpn.android.models.Ad
import java.security.InvalidParameterException


class ViewAdsContract : ActivityResultContract<Ad, Ad?>() {

    private lateinit var context: Context

    override fun createIntent(context: Context, input: Ad?): Intent {
        this.context = context
        input?.let {
            return AdViewingActivity.makeIntent(context ,it)
        } ?: throw InvalidParameterException()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Ad? {
        if (resultCode != Activity.RESULT_OK) return null
        if (intent == null) return null

        return intent.getParcelableExtra(AdViewingActivity.AD_ACTIVITY_OPERATION_RESULT)
    }


}