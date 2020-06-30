/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ekovpn.android.R
import com.ekovpn.android.data.config.model.Protocol
import com.ekovpn.android.di.main.settings.DaggerSettingsComponent
import com.ekovpn.android.di.main.settings.SettingsModule
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class SettingsFragment : Fragment() {

    @Inject
    lateinit var viewModel: SettingsViewModel


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.state
                .onEach {
                    delay(1000)
                    render(it)
                }
                .launchIn(lifecycleScope)


        protocol_group.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.tcp -> {
                    viewModel.selectProtocol(Protocol.TCP)
                }
                R.id.udp -> {
                    viewModel.selectProtocol(Protocol.UDP)
                }
                R.id.ikev2 -> {
                    viewModel.selectProtocol(Protocol.IKEV2)
                }
                else -> {
                    viewModel.selectProtocol(Protocol.WIREGUARD)
                }
            }
        }
    }

    private fun render(it: SettingsState) {
        selectCurrentProtocol(it)
    }

    private fun selectCurrentProtocol(it: SettingsState) {
        when (it.selectedProtocol) {
            Protocol.TCP -> {
                protocol_group.check(R.id.tcp)
            }
            Protocol.UDP -> {
                protocol_group.check(R.id.tcp)
            }
            Protocol.IKEV2 -> {
                protocol_group.check(R.id.ikev2)
            }
            Protocol.WIREGUARD -> {
                protocol_group.check(R.id.wire_guard)
            }
        }
    }


    private fun injectDependencies() {
        DaggerSettingsComponent
                .builder()
                .settingsModule(SettingsModule(this))
                .vPNComponent(vpnComponent(requireActivity()))
                .build()
                .inject(this)
    }
}