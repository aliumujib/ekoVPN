/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.ekovpn.android.ApplicationClass
import com.ekovpn.android.R
import com.ekovpn.android.di.main.DaggerVPNComponent
import com.ekovpn.android.di.main.VPNComponent
import com.ekovpn.android.di.main.VPNModule
import com.ekovpn.android.utils.ext.hide
import com.ekovpn.android.utils.ext.show
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_vpn.*


class VpnActivity : AppCompatActivity() {

    lateinit var vpnComponent: VPNComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setContentView(R.layout.activity_vpn)


        setSupportActionBar(findViewById(R.id.toolbar))
        val navController = findNavController(this@VpnActivity, R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.HomeFragment) {
                logo_bar.hide()
                app_bar.show()
            } else {
                logo_bar.show()
                app_bar.hide()
            }
        }

        toolbar.setNavigationOnClickListener {
            navController.navigateUp() || super.onSupportNavigateUp()
        }

        if (intent?.action?.equals(VIEW_MORE_ADS_ACTION) == true) {
            toolbar.title = getString(R.string.ads_fragment_label)
            findNavController(this@VpnActivity, R.id.nav_host_fragment).navigate(R.id.AdsFragment)
        }

        checkForUpdate()
    }


    private fun checkForUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        1234)
            }
        }
    }


    private fun injectDependencies() {
        vpnComponent = DaggerVPNComponent
                .builder()
                .coreComponent(ApplicationClass.coreComponent(this))
                .vPNModule(VPNModule(this))
                .build()

        vpnComponent.inject(this)
    }


    companion object {

        /**
         * Obtain core dagger component.
         *
         * @param activity The host activity
         */
        @JvmStatic
        fun vpnComponent(activity: Activity) = (activity as VpnActivity).vpnComponent


        const val VIEW_MORE_ADS_ACTION = "VIEW_MORE_ADS_ACTION"
        const val BUY_PREMIUM_ACTION = "BUY_PREMIUM_ACTION"

    }

}