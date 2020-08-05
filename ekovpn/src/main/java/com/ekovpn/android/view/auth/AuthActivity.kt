/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ekovpn.android.ApplicationClass.Companion.coreComponent
import com.ekovpn.android.R
import com.ekovpn.android.di.auth.AuthComponent
import com.ekovpn.android.di.auth.AuthModule
import com.ekovpn.android.di.auth.DaggerAuthComponent
import com.ekovpn.android.view.auth.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class AuthActivity : AppCompatActivity() {

    lateinit var authComponent: AuthComponent

    @Inject
    lateinit var viewModel: AuthViewModel


    private val pagerAdapter by lazy {
        ViewPagerAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        injectDependencies()

        pager.isUserInputEnabled = false
        pager.adapter = pagerAdapter

        val tabLayoutMediator = TabLayoutMediator(tab_layout, pager, true, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.view.isClickable = false;
        })
        tabLayoutMediator.attach()

        viewModel
                .navigation
                .onEach {
                    handleNavCommands(it)
                }.launchIn(lifecycleScope)

    }

    private fun handleNavCommands(it: NavCommand) {
        when(it){
            NavCommand.Stay -> {
            }
            NavCommand.GoToSuccessCommand -> {
                pager.currentItem = 1
            }
            NavCommand.GoToLoginCommand -> {
                pager.currentItem = 0
            }
        }
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
        fun authComponent(activity: Activity) = (activity as AuthActivity).authComponent

    }

}