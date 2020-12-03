/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.downloader.PRDownloader
import com.ekovpn.android.di.app.DaggerApplicationComponent
import com.ekovpn.android.di.components.CoreComponent
import com.ekovpn.android.di.components.DaggerCoreComponent
import com.ekovpn.android.di.modules.ContextModule
import com.ekovpn.android.scheduling.TimeResetWorkerWM
import com.ekovpn.wireguard.WireGuardInitializer
import com.google.android.gms.ads.MobileAds
import com.jirbo.adcolony.AdColonyBundleBuilder
import com.onesignal.OneSignal
import de.blinkt.openvpn.core.ICSOpenVPNApplication
import org.strongswan.android.security.LocalCertificateKeyStoreProvider
import org.strongswan.android.utils.ContextProvider
import java.security.Security
import java.util.concurrent.TimeUnit

class ApplicationClass : ICSOpenVPNApplication(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration =
            Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .build()

    lateinit var coreComponent: CoreComponent

//    private val listOfAdUnitIds = mutableListOf(
//            getString(R.string.interstitial_zone_id),
//            getString(R.string.banner_zone_id),
//            getString(R.string.rewarded_zone_id)
//    )


    init {
        instance = this
    }

    init {
        Security.addProvider(LocalCertificateKeyStoreProvider())
        System.loadLibrary("androidbridge")
    }


    override fun onCreate() {
        super.onCreate()
        initCoreDependencyInjection()
        initAppDependencyInjection()
        initNotificationChannels()
        initPushNotifications()
        ContextProvider.setContext(applicationContext)
        PRDownloader.initialize(applicationContext)
        initAdmob()
        initWorkManager()

        AdColonyBundleBuilder.setShowPrePopup(true)
        AdColonyBundleBuilder.setShowPostPopup(true)

        WireGuardInitializer.onCreate(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    }

    private fun initWorkManager() {
        val constraintsBuilder = Constraints.Builder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            constraintsBuilder.setRequiresDeviceIdle(true)
        }

        val repeatInterval = if (BuildConfig.DEBUG) {
            24L
        } else {
            24L
        }

        val timeUnit = if (BuildConfig.DEBUG) {
            TimeUnit.HOURS
        } else {
            TimeUnit.HOURS
        }


        val constraints = constraintsBuilder.build()
        val work = PeriodicWorkRequestBuilder<TimeResetWorkerWM>(repeatInterval, timeUnit)
                .setConstraints(constraints)
                .build()
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(work)
    }


    private fun initPushNotifications() {
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
    }

    private fun initAdmob() {
        MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID)
    }

    private fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    EKO_NOTIFICATION_CHANNEL_ID,
                    EKO_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.setSound(null, null);
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }


    /**
     * Initialize core dependency injection component.
     */
    private fun initAppDependencyInjection() {
        DaggerApplicationComponent
                .builder()
                .coreComponent(coreComponent)
                .build()
                .inject(this)
    }

    /**
     * Initialize core dependency injection component.
     */
    private fun initCoreDependencyInjection() {
        coreComponent = DaggerCoreComponent
                .builder()
                .contextModule(ContextModule(this))
                .build()
    }

    companion object {

        private var instance: ApplicationClass? = null

        fun getInstance(): Context? {
            return instance?.applicationContext
        }

        /**
         * Obtain core dagger component.
         *
         * @param context The application context
         */
        @JvmStatic
        fun coreComponent(context: Context) = (context.applicationContext as? ApplicationClass)?.coreComponent

        const val EKO_NOTIFICATION_CHANNEL_NAME = "EKO_NOTIFICATION_CHANNEL_NAME"
        const val EKO_NOTIFICATION_CHANNEL_ID = "EKO_NOTIFICATION_CHANNEL_ID"
    }

}