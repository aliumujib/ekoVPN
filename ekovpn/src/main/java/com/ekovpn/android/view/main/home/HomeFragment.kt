/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.api.load
import com.ekovpn.android.R
import com.ekovpn.android.di.main.home.DaggerHomeComponent
import com.ekovpn.android.di.main.home.HomeModule
import com.ekovpn.android.models.Server
import com.ekovpn.android.utils.ext.show
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import com.ekovpn.android.view.main.locationselector.LocationSelectorDialog
import de.blinkt.openvpn.LaunchVPN
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.activities.DisconnectVPN
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VpnStatus
import de.blinkt.openvpn.core.VpnStatus.StateListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class HomeFragment : Fragment(), StateListener {

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

        select_location_card.children.forEach {
            it.setOnClickListener {
                LocationSelectorDialog.display(childFragmentManager, object : LocationSelectorDialog.ClicksListener {
                    override fun onClose() {

                    }

                    override fun onBackPressed() {

                    }

                    override fun onLocationSelected(server: Server) {
                        initCurrentConnectionUI(server)
                        if (server is Server.OVPNServer) {
                            startOrStopVPN(ProfileManager.get(requireContext(), server.ovpnProfileId))
                            viewModel.connectingToServer(server)
                        }
                    }

                }, viewModel.state.value._serversList)
            }
        }

        viewModel.state.onEach {
            render(it)
        }.launchIn(lifecycleScope)


        connect.setOnClickListener {
            val currentServer = viewModel.state.value.lastUsedServer
            if (currentServer != null) {
                if (currentServer is Server.OVPNServer) {
                    startOrStopVPN(ProfileManager.get(requireContext(), currentServer.ovpnProfileId))
                    viewModel.connectingToServer(currentServer)
                }
            }else{
                Toast.makeText(context,"Please pick a location", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        VpnStatus.addStateListener(this)
    }

    private fun initCurrentConnectionUI(server: Server) {
        selected_flag.load("https://www.countryflags.io/${server.location_.country_code}/flat/64.png")
        selected_location.text = Html.fromHtml("${server.location_.city}-${server.location_.country}")
    }

    private fun initLastSelectedUI(server: Server) {
        country_flag.load("https://www.countryflags.io/${server.location_.country_code}/flat/64.png")
        location_name.text = Html.fromHtml("${server.location_.city}-${server.location_.country}")
    }

    private fun render(it: HomeState) {
        when (it.connectionStatus) {
            HomeState.ConnectionStatus.DISCONNECTED -> {
                connect.setStrokeColorResource(R.color.eko_red_light)
                connection_status_.text = resources.getString(R.string.disconnected_status_)
                connect.isEnabled = true
            }
            HomeState.ConnectionStatus.CONNECTING -> {
                connection_status_.text = resources.getString(R.string.connecting_status_)
                connect.setStrokeColorResource(R.color.grey)
                connect.isEnabled = false
            }
            HomeState.ConnectionStatus.CONNECTED -> {
                connection_status_.text = resources.getString(R.string.connected_status_)
                connect.setStrokeColorResource(R.color.connected_green)
                connect.isEnabled = true
            }
        }

        if (it.currentConnectionServer == null) {
            //get location via IP and use that
        } else {
            initCurrentConnectionUI(it.currentConnectionServer)
        }

        it.lastUsedServer?.let {
            select_location_card.show()
            initLastSelectedUI(it)
            country_flag.show()
            location_name.show()
        }
    }


    private fun startOrStopVPN(profile: VpnProfile) {
        if (VpnStatus.isVPNActive() && profile.uuidString == VpnStatus.getLastConnectedVPNProfile()) {
            val disconnectVPN = Intent(activity, DisconnectVPN::class.java)
            startActivity(disconnectVPN)
        } else {
            startVPN(profile)
        }
    }


    private fun startVPN(profile: VpnProfile) {
        val intent = Intent(activity, LaunchVPN::class.java)
        intent.putExtra(LaunchVPN.EXTRA_KEY, profile.uuid.toString())
        intent.action = Intent.ACTION_MAIN
        startActivity(intent)
    }

    private fun injectDependencies() {
        DaggerHomeComponent
                .builder()
                .homeModule(HomeModule(this))
                .vPNComponent(vpnComponent(requireActivity()))
                .build()
                .inject(this)
    }

    override fun updateState(state: String?, logmessage: String?, localizedResId: Int, level: ConnectionStatus?, Intent: Intent?) {
        if (level == ConnectionStatus.LEVEL_CONNECTED) {
            viewModel.setConnected()
        } else if (level == ConnectionStatus.LEVEL_NOTCONNECTED) {
            viewModel.setDisconnected()
        }
    }

    override fun setConnectedVPN(uuid: String?) {

    }

}