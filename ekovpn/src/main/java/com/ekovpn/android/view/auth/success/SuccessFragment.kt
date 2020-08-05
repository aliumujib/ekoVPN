/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.success

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.ekovpn.android.R
import com.ekovpn.android.di.auth.success.DaggerSuccessComponent
import com.ekovpn.android.di.auth.success.SuccessModule
import com.ekovpn.android.utils.ext.copyToClipBoard
import com.ekovpn.android.utils.ext.hide
import com.ekovpn.android.view.auth.AuthActivity.Companion.authComponent
import com.ekovpn.android.view.auth.AuthState
import com.ekovpn.android.view.auth.AuthViewModel
import com.ekovpn.android.view.main.VpnActivity
import kotlinx.android.synthetic.main.fragment_success.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class SuccessFragment : Fragment() {


    @Inject
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    private fun injectDependencies() {
        DaggerSuccessComponent
                .builder()
                .authComponent(authComponent(requireActivity()))
                .successModule(SuccessModule(this))
                .build()
                .inject(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state
                .onEach {
                    handleState(it)
                }
                .launchIn(lifecycleScope)

        initViews()
    }

    private fun initViews() {
        start_free.setOnClickListener {
            val intent = Intent(requireContext(), VpnActivity::class.java)
            startActivity(intent)
            this.requireActivity().finish()
        }

        account_number.setOnClickListener {
            viewModel.state.value?.user?.account_id?.let {
                context?.copyToClipBoard(it)
            }
            Toast.makeText(requireContext(), getString(R.string.account_number_copied), Toast.LENGTH_LONG).show()
        }
    }

    private fun insertPeriodically(
            text: String, insert: String, period: Int): String? {
        val builder = StringBuilder(
                text.length + insert.length * (text.length / period) + 1)
        var index = 0
        var prefix = ""
        while (index < text.length) {
            // Don't put the insert in the very first iteration.
            // This is easier than appending it *after* each substring
            builder.append(prefix)
            prefix = insert
            builder.append(text.substring(index,
                    Math.min(index + period, text.length)))
            index += period
        }
        return builder.toString()
    }

    private fun handleState(state: AuthState) {
        state.user?.account_id?.let {
            account_number.text = insertPeriodically(it, " ", 4)
        }
        if(state.user?.account_type == "paid"){
            welcome_intro_text.text = getString(R.string.welcome_back)
            free_to_use_text.hide()
            start_free.hide()
            premium_options.hide()
        }else{
            if(state.isFreshAccount){
                welcome_intro_text.text = getString(R.string.keep_acct_number_safe)
                start_free.text = getString(R.string.start_free)
            }else{
                welcome_intro_text.text = getString(R.string.welcome_back)
                start_free.text = getString(R.string.continue_free)
            }
        }
    }

}