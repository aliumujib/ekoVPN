/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ekovpn.android.ApplicationClass.Companion.coreComponent
import com.ekovpn.android.R
import com.ekovpn.android.data.config.downloader.FileDownloader
import com.ekovpn.android.data.config.importer.OVPNProfileImporter
import com.ekovpn.android.data.config.repository.ConfigRepository
import com.ekovpn.android.data.config.repository.ConfigRepositoryImpl
import com.ekovpn.android.di.splash.DaggerSplashComponent
import com.ekovpn.android.di.splash.SplashModule
import com.ekovpn.android.utils.ext.hide
import com.ekovpn.android.utils.ext.observe
import com.ekovpn.android.utils.ext.show
import de.blinkt.openvpn.activities.MainActivity
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setContentView(R.layout.activity_splash)

        splashViewModel
                .state
                .onEach {
                    delay(1000)
                    render(it)
                }
                .launchIn(lifecycleScope)
    }


    private fun render(state: SetUpState) {
        when (state) {
            SetUpState.Working -> {
                progressBar.show()
                setup_text_.show()
                setup_text_.setText(R.string.wait_while_we_set_up)
            }
            SetUpState.Finished -> {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                this@SplashActivity.finish()
            }
            SetUpState.Failed -> {
                setup_text_.setText(R.string.an_error_occured)
                Log.d(SplashActivity::class.java.simpleName, "Error")
            }
            else -> {
                progressBar.hide()
                setup_text_.hide()
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