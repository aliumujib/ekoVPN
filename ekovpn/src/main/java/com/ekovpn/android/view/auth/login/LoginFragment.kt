/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.login

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ekovpn.android.R
import com.ekovpn.android.di.auth.login.DaggerLoginComponent
import com.ekovpn.android.di.auth.login.LoginModule
import com.ekovpn.android.view.auth.SplashActivity.Companion.authComponent
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class LoginFragment : Fragment() {

    @Inject
    lateinit var loginViewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    private fun injectDependencies() {
        DaggerLoginComponent
                .builder()
                .authComponent(authComponent(requireActivity()))
                .loginModule(LoginModule(this))
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    private fun showProgress() {
        login_btn.isEnabled = false
        sign_up.isEnabled = false
    }

    private fun hideProgress() {
        login_btn.isEnabled = true
        sign_up.isEnabled = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        loginViewModel.state.onEach {
            handleState(it)
        }.launchIn(lifecycleScope)
    }

    private fun initViews() {
        existing_user.text = Html.fromHtml(getString(R.string.existing_title))
        login_btn.setOnClickListener {
            loginViewModel.login(account_number_input.text.toString())
        }
        sign_up.setOnClickListener {
            loginViewModel.createAccount()
        }
    }

    private fun handleState(state: LoginState) {
        when (state) {
            LoginState.Idle -> {

            }
            LoginState.Working -> {
                showProgress()
            }
            is LoginState.Finished -> {
                hideProgress()
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSuccessFragment(state.isFreshAccount))
            }
            is LoginState.Failed -> {
                hideProgress()
                Toast.makeText(context, state.error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}