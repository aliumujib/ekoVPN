/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.splashscreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ekovpn.android.R
import com.ekovpn.android.di.auth.splash.DaggerSplashComponent
import com.ekovpn.android.di.auth.splash.SplashModule
import com.ekovpn.android.view.auth.SplashActivity.Companion.authComponent
import com.ekovpn.android.view.main.VpnActivity
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class SplashFragment : Fragment() {


    @Inject
    lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    private fun injectDependencies() {
        DaggerSplashComponent
                .builder()
                .authComponent(authComponent(requireActivity()))
                .splashModule(SplashModule(this))
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state
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
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
            }
            SetUpState.Failed -> {
                retry.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                setup_text_.setText(R.string.an_error_occured)
                Log.d(SplashFragment::class.java.simpleName, "Error")
            }
            else -> {
                retry.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                setup_text_.visibility = View.INVISIBLE
            }
        }
    }

}