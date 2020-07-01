/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.ekovpn.android.R
import com.ekovpn.android.di.main.home.DaggerHomeComponent
import com.ekovpn.android.di.main.home.HomeModule
import com.ekovpn.android.di.main.settings.DaggerSettingsComponent
import com.ekovpn.android.di.main.settings.SettingsModule
import com.ekovpn.android.view.main.VpnActivity
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectDependencies()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    private fun injectDependencies() {
        DaggerHomeComponent
                .builder()
                .homeModule(HomeModule(this))
                .vPNComponent(vpnComponent(requireActivity()))
                .build()
                .inject(this)
    }
}