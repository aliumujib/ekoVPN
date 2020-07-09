/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android


import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.annotation.RequiresApi
import com.downloader.PRDownloader
import com.ekovpn.android.di.app.DaggerApplicationComponent
import com.ekovpn.android.di.components.CoreComponent
import com.ekovpn.android.di.components.DaggerCoreComponent
import com.ekovpn.android.di.modules.ContextModule
import com.ekovpn.android.utils.detectAllExpect
import de.blinkt.openvpn.core.ICSOpenVPNApplication
import org.strongswan.android.security.LocalCertificateKeyStoreProvider
import org.strongswan.android.ui.LaunchActivity
import org.strongswan.android.utils.ContextProvider
import java.security.Security


class ApplicationClass: ICSOpenVPNApplication() {


    lateinit var coreComponent: CoreComponent

    init {
        Security.addProvider(LocalCertificateKeyStoreProvider())
        System.loadLibrary("androidbridge")
    }

    override fun onCreate() {
        super.onCreate()

        initCoreDependencyInjection()
        initAppDependencyInjection()
        handleAndroidOStrictModeViolations()
        ContextProvider.setContext(applicationContext)
        PRDownloader.initialize(getApplicationContext());
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

        /**
         * Obtain core dagger component.
         *
         * @param context The application context
         */
        @JvmStatic
        fun coreComponent(context: Context) =
                (context.applicationContext as? ApplicationClass)?.coreComponent
    }

}