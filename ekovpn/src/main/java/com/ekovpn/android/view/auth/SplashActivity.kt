/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.ekovpn.android.ApplicationClass.Companion.coreComponent
import com.ekovpn.android.R
import com.ekovpn.android.di.auth.DaggerAuthComponent
import com.ekovpn.android.di.auth.AuthComponent
import com.ekovpn.android.di.auth.AuthModule
import com.ekovpn.android.utils.ext.hide
import com.ekovpn.android.utils.ext.show
import com.ekovpn.android.view.auth.splashscreen.SetUpState
import com.ekovpn.android.view.auth.splashscreen.SplashViewModel
import com.ekovpn.android.view.main.VpnActivity
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_vpn.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SplashActivity : AppCompatActivity() {

    lateinit var authComponent: AuthComponent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setContentView(R.layout.activity_splash)

        val navController = Navigation.findNavController(this@SplashActivity, R.id.nav_host_fragment)

    }


    private fun injectDependencies() {
        authComponent = DaggerAuthComponent
                .builder()
                .coreComponent(coreComponent(this))
                .authModule(AuthModule(this))
                .build()

        authComponent.inject(this)
    }


    companion object {

        /**
         * Obtain core dagger component.
         *
         * @param activity The host activity
         */
        @JvmStatic
        fun authComponent(activity: Activity) = (activity as SplashActivity).authComponent

    }

}