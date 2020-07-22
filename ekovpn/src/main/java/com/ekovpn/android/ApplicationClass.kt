/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.StrictMode
import androidx.annotation.RequiresApi
import com.downloader.PRDownloader
import com.ekovpn.android.di.app.DaggerApplicationComponent
import com.ekovpn.android.di.components.CoreComponent
import com.ekovpn.android.di.components.DaggerCoreComponent
import com.ekovpn.android.di.modules.ContextModule
import com.ekovpn.android.utils.detectAllExpect
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import de.blinkt.openvpn.core.ICSOpenVPNApplication
import org.strongswan.android.security.LocalCertificateKeyStoreProvider
import org.strongswan.android.utils.ContextProvider
import java.security.Security
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener


class ApplicationClass: ICSOpenVPNApplication(), SharedPreferences.OnSharedPreferenceChangeListener {


    lateinit var coreComponent: CoreComponent


    init {
        instance = this
    }

    init {
        Security.addProvider(LocalCertificateKeyStoreProvider())
        System.loadLibrary("androidbridge")
    }


    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
//        if (BuildConfig.MIN_SDK_VERSION > Build.VERSION.SDK_INT) {
//            val intent = Intent(Intent.ACTION_MAIN)
//            intent.addCategory(Intent.CATEGORY_HOME)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            exitProcess(0)
//        }
//        if (BuildConfig.DEBUG) {
//            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
//        }
    }

    override fun onCreate() {
        super.onCreate()

        initCoreDependencyInjection()
        initAppDependencyInjection()
        initNotificationChannels()
        handleAndroidOStrictModeViolations()
        ContextProvider.setContext(applicationContext)
        PRDownloader.initialize(applicationContext)
        initAdmob()
        //WireGuardInitializer.onCreate(this)
        //WireGuardInitializer.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
//        if ("multiple_tunnels" == key && WireGuardInitializer.getBackend() is WgQuickBackend)
//            (WireGuardInitializer.getBackend() as WgQuickBackend).setMultipleTunnels(sharedPreferences.getBoolean(key, false))
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleAndroidOStrictModeViolations() {
        StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                        .detectAllExpect("android.os.StrictMode.onUntaggedSocket")
                        .build())

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