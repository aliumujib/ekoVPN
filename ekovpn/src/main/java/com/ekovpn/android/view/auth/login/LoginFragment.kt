/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.login

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ekovpn.android.R
import com.ekovpn.android.di.auth.login.DaggerLoginComponent
import com.ekovpn.android.di.auth.login.LoginModule
import com.ekovpn.android.utils.ext.hideKeyboard
import com.ekovpn.android.utils.input.CreditCardMask
import com.ekovpn.android.view.auth.AuthActivity.Companion.authComponent
import com.ekovpn.android.view.auth.AuthState
import com.ekovpn.android.view.auth.AuthViewModel
import com.ekovpn.android.view.auth.accountnumberbottomsheeet.AccountRecoveryBottomSheet
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class LoginFragment : Fragment() {

    @Inject
    lateinit var authViewModel: AuthViewModel

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
        progressBar.visibility = View.VISIBLE
        login_btn.visibility = View.GONE
        sign_up.visibility = View.GONE
        forgot_account_number.visibility = View.GONE
        new_user.visibility = View.GONE
        login_btn.isEnabled = false
        sign_up.isEnabled = false
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
        login_btn.visibility = View.VISIBLE
        sign_up.visibility = View.VISIBLE
        new_user.visibility = View.VISIBLE
        forgot_account_number.visibility = View.VISIBLE
        login_btn.isEnabled = true
        sign_up.isEnabled = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        authViewModel.state.onEach {
            handleState(it)
        }.launchIn(lifecycleScope)
    }

    private fun initViews() {
        existing_user.text = Html.fromHtml(getString(R.string.existing_title))
        account_number_input.requestFocus()
        CreditCardMask(account_number_input).listen()
        login_btn.setOnClickListener {
            hideKeyboard()
            authViewModel.login(account_number_input.text.toString())
        }
        sign_up.setOnClickListener {
            authViewModel.createAccount()
        }
        forgot_account_number.setOnClickListener {
            showBottomSheet()
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun showBottomSheet() {
        AccountRecoveryBottomSheet() {
            authViewModel.recoverAccount(it)
        }.show(childFragmentManager, javaClass.simpleName)
    }

    private fun handleState(state: AuthState) {
        Log.d(LoginFragment::class.java.simpleName, state.toString())
        if(state.isLoading){
            showProgress()
        }else if (state.user != null && state.hasCompletedConfig && state.isLoading.not()){
            hideProgress()
            authViewModel.goToSuccessScreen()
        }else if(state.error != null){
            hideProgress()
            Toast.makeText(context, state.error.message, Toast.LENGTH_LONG).show()
        }
    }

}