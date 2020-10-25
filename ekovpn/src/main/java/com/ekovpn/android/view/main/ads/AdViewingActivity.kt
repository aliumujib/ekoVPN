/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.ads

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ekovpn.android.R
import com.ekovpn.android.models.Ad
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdViewingActivity : AppCompatActivity() {


    private val listOfAdUnitIds = mutableListOf(R.string.rewarded_ad_1,
            R.string.rewarded_ad_2, R.string.rewarded_ad_3,
            R.string.rewarded_ad_4, R.string.rewarded_ad_5,
            R.string.rewarded_ad_6, R.string.rewarded_ad_7,
            R.string.rewarded_ad_8, R.string.rewarded_ad_9,
            R.string.rewarded_ad_10, R.string.rewarded_ad_11,
            R.string.rewarded_ad_12, R.string.rewarded_ad_13,
            R.string.rewarded_ad_13, R.string.rewarded_ad_13, R.string.rewarded_ad_13,
            R.string.rewarded_ad_13, R.string.rewarded_ad_13, R.string.rewarded_ad_13,
            R.string.rewarded_ad_13, R.string.rewarded_ad_13, R.string.rewarded_ad_13,
            R.string.rewarded_ad_13
    )

    var current = 0
    val ad: Ad by lazy {
        intent.getParcelableExtra<Ad>(AD_PARAM)
    }
    var adLeaveCheck: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_viewing)

        createAndLoadRewardedAd(resources.getString(listOfAdUnitIds[current]))
        current += 1

    }

    private fun getCallback(): RewardedAdCallback {
        return object : RewardedAdCallback() {
            override fun onRewardedAdOpened() {
                // Ad opened.
                adLeaveCheck = 1
                Log.d(AdsFragment::class.java.simpleName, "Ad opened")
            }

            override fun onRewardedAdClosed() {
                // Ad closed.
                Log.d(AdsFragment::class.java.simpleName, "Ad $current closed")
                if (current < ad.count && adLeaveCheck == 2) {
                    current += 1
                    createAndLoadRewardedAd(resources.getString(listOfAdUnitIds[current]))
                } else if (current == ad.count && adLeaveCheck == 2) {
                    val intent = Intent()
                    intent.putExtra(AD_ACTIVITY_OPERATION_RESULT, ad)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    val intent = Intent()
                    setResult(Activity.RESULT_CANCELED, intent)
                    finish()
                }
            }

            override fun onUserEarnedReward(p0: RewardItem) {
                adLeaveCheck = 2
                Toast.makeText(this@AdViewingActivity, "You can now close this ad", Toast.LENGTH_LONG).show()
                Log.d(AdsFragment::class.java.simpleName, "Ad reward earn $p0")
            }

            override fun onRewardedAdFailedToShow(errorCode: Int) {
                // Ad failed to display.
                Log.d(AdsFragment::class.java.simpleName, "Ad failed to show $errorCode")
            }
        }
    }

    private fun createAndLoadRewardedAd(adUnitId: String): RewardedAd {
        val rewardedAd = RewardedAd(this, adUnitId)
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
                Log.d(AdsFragment::class.java.simpleName, "Ad loaded")
                rewardedAd.show(this@AdViewingActivity, getCallback())
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                // Ad failed to load.
                Log.d(AdsFragment::class.java.simpleName, "failed to load $errorCode")
            }
        }
        //RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("CF69F6487BD2BF4E9F46D152C82B6FD6"))
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }


    companion object {
        const val AD_PARAM = "AD_PARAM"
        const val AD_ACTIVITY_INTENT_KEY = 302
        const val AD_ACTIVITY_OPERATION_RESULT = "AD_ACTIVITY_OPERATION_RESULT"

        fun makeIntent(context: Context, ad: Ad): Intent {
            val intent = Intent(context, AdViewingActivity::class.java)
            intent.putExtra(AD_PARAM, ad)
            return intent
        }
    }
}