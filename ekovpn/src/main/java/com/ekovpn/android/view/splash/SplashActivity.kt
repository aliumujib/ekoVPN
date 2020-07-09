/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.splash

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ekovpn.android.ApplicationClass.Companion.coreComponent
import com.ekovpn.android.R
import com.ekovpn.android.di.splash.DaggerSplashComponent
import com.ekovpn.android.di.splash.SplashModule
import com.ekovpn.android.utils.ext.hide
import com.ekovpn.android.utils.ext.show
import com.ekovpn.android.view.main.VpnActivity
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setContentView(R.layout.activity_splash)

        viewModel
                .state
                .onEach {
                    delay(1000)
                    render(it)
                }
                .launchIn(lifecycleScope)

        retry.setOnClickListener {
            viewModel.runSetupIfNeeded()
        }
    }


    private fun render(state: SetUpState) {
        when (state) {
            SetUpState.Working -> {
                progressBar.visibility = View.VISIBLE
                setup_text_.visibility = View.VISIBLE
                retry.visibility = View.GONE
                setup_text_.setText(R.string.wait_while_we_set_up)
            }
            SetUpState.Finished -> {
                val intent = Intent(this@SplashActivity, VpnActivity::class.java)
                startActivity(intent)
                this@SplashActivity.finish()
            }
            SetUpState.Failed -> {
                retry.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                setup_text_.setText(R.string.an_error_occured)
                Log.d(SplashActivity::class.java.simpleName, "Error")
            }
            else -> {
                retry.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                setup_text_.visibility = View.INVISIBLE
            }
        }
    }

    private fun injectDependencies() {
        DaggerSplashComponent
                .builder()
                .coreComponent(coreComponent(this))
                .splashModule(SplashModule(this))
                .build()
                .inject(this)
    }

}