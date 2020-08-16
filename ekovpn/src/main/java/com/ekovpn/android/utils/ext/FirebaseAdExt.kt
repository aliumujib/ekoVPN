/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.utils.ext

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ekovpn.android.view.main.ads.AdsFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

 fun Activity.createAndLoadRewardedAd(adUnitId: String, callback: RewardedAdCallback): RewardedAd {
    val instance = this
    val rewardedAd = RewardedAd(this, adUnitId)
    val adLoadCallback = object : RewardedAdLoadCallback() {
        override fun onRewardedAdLoaded() {
            // Ad successfully loaded.
            Log.d(AdsFragment::class.java.simpleName, "Ad loaded")
            rewardedAd.show(instance, callback)
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