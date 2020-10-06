/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.utils.ext

import android.app.Activity
import android.util.Log
import com.ekovpn.android.view.main.ads.AdsFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
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


fun Activity.createAndLoadInterstitialAd(adUnitId: String): InterstitialAd {
    val instance = this
    val interstitialAd = InterstitialAd(this)
    interstitialAd.adUnitId = adUnitId
    val adLoadCallback = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            Log.d(AdsFragment::class.java.simpleName, "Ad loaded")
            interstitialAd.show()
        }

        override fun onAdFailedToLoad(p0: LoadAdError?) {
            super.onAdFailedToLoad(p0)
            Log.d(AdsFragment::class.java.simpleName, "failed to load $p0")
        }

    }
    interstitialAd.adListener = adLoadCallback
    //RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("CF69F6487BD2BF4E9F46D152C82B6FD6"))
    interstitialAd.loadAd(AdRequest.Builder().build())
    return interstitialAd
}