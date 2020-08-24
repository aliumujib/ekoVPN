/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.home

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.ekovpn.android.ApplicationClass
import com.ekovpn.android.R
import com.ekovpn.android.di.main.home.DaggerHomeComponent
import com.ekovpn.android.di.main.home.HomeModule
import com.ekovpn.android.models.Location
import com.ekovpn.android.models.Server
import com.ekovpn.android.service.EkoVPNMgrService
import com.ekovpn.android.utils.ext.*
import com.ekovpn.android.view.compoundviews.countdowntimer.TimeMilliParser
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import com.ekovpn.android.view.main.locationselector.LocationSelectorDialog
import com.ekovpn.android.view.main.profile.ProfileDialog
import com.ekovpn.android.view.main.webview.WebViewDialog
import com.ekovpn.wireguard.WireGuardInitializer
import com.ekovpn.wireguard.service.WireGuardService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import de.blinkt.openvpn.LaunchVPN
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.activities.DisconnectVPN
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.VpnStatus
import de.blinkt.openvpn.core.VpnStatus.StateListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.logic.VpnStateService.LocalBinder
import org.strongswan.android.ui.VpnProfileControlActivity
import javax.inject.Inject


@ExperimentalCoroutinesApi
class HomeFragment : Fragment(), StateListener, VpnStateService.VpnStateListener, EkoVPNMgrService.TimeLeftListener, WireGuardService.WireGuardListener {

    @Inject
    lateinit var viewModel: HomeViewModel

    private var iKEv2Service: VpnStateService? = null
    private var ekoVpnMgrService: EkoVPNMgrService? = null
    private var wireGuardService: WireGuardService? = null

    private var balloon:Balloon?= null

    private val getUserVPNPermission = registerForActivityResult(GetWireGuardVPNPermissions()) {
        if (it) {

        } else {
            Toast.makeText(context, "An error occurred while getting permissions", Toast.LENGTH_LONG).show()
        }
    }

    private val mIKEv2ServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            iKEv2Service = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            iKEv2Service = (service as LocalBinder).service
            iKEv2Service?.registerListener(this@HomeFragment)
            checkForExistingConnection()
        }
    }


    private val mTimerServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            ekoVpnMgrService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            ekoVpnMgrService = (service as EkoVPNMgrService.VPNTimerLocalBinder).getService()
            ekoVpnMgrService?.registerListener(this@HomeFragment)
            checkForExistingConnection()
        }
    }


    private val mWireGuardServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            wireGuardService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            wireGuardService = (service as WireGuardService.WireGuardServiceLocalBinder).getService()
            wireGuardService?.registerListener(this@HomeFragment)
            checkForExistingConnection()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectDependencies()
        bindToServices()
    }

    private fun bindToServices() {
        /* bind to the service only seems to work from the ApplicationContext */
        ApplicationClass.getInstance()?.let {
            it.bindService(Intent(it, VpnStateService::class.java), mIKEv2ServiceConnection, Service.BIND_AUTO_CREATE)
            it.bindService(Intent(it, EkoVPNMgrService::class.java), mTimerServiceConnection, Service.BIND_AUTO_CREATE)
            it.bindService(Intent(it, WireGuardService::class.java), mWireGuardServiceConnection, Service.BIND_AUTO_CREATE)
        }
    }

    override fun onTimeUpdate(timeLeftMillis: Long, timeLeftFormatted: String) {
        stopBlinkingAnimation()
        timer_view.text = timeLeftFormatted
        if (timeLeftMillis < 120000) {
            startBlinkingAnimation()
        } else if (timeLeftMillis <= 1L) {
            stopBlinkingAnimation()
        }
        Log.d(HomeFragment::class.java.simpleName, "$timeLeftMillis, $timeLeftFormatted")
    }

    override fun onStart() {
        super.onStart()
        iKEv2Service?.registerListener(this)
        ekoVpnMgrService?.registerListener(this)
        wireGuardService?.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        iKEv2Service?.unregisterListener(this)
        ekoVpnMgrService?.unregisterListener(this)
        wireGuardService?.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        stopBlinkingAnimation()
        balloon?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        ApplicationClass.getInstance()?.let {
            it.unbindService(mTimerServiceConnection)
            it.unbindService(mIKEv2ServiceConnection)
            it.unbindService(mWireGuardServiceConnection)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLocationPicker()
        viewModel.fetchLocationForCurrentIP()
        observeStates()
        initButtonClickListeners()
        initAdControls()
    }

    private fun initAdControls() {
        delay({
            if (viewModel.shouldShowAds()) {
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
            }
        })
    }

    private fun initButtonClickListeners() {
        connect.setOnClickListener {
            val server = viewModel.state.value.lastUsedServer
            val hasTimeLeft = viewModel.state.value.timeLeft > 0
            Log.d(HomeFragment::class.java.simpleName, "${viewModel.state.value}")
            if (hasTimeLeft.not() && viewModel.shouldShowAds()) {
                Toast.makeText(context, "Please view some ads or buy premium", Toast.LENGTH_LONG).show()
                goToViewAdsScreen()
            } else if (server == null) {
                Toast.makeText(context, "Please pick a location", Toast.LENGTH_LONG).show()
            } else {
                connectToServer(server)
            }
        }

        disconnect.setOnClickListener {
            viewModel.state.value.currentConnectionServer?.let {
                connectToServer(it)
            }
        }

        activity?.findViewById<View>(R.id.settings_btn)?.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettingsFragment())
        }

        activity?.findViewById<View>(R.id.profile_btn)?.setOnClickListener {
            ProfileDialog.display(childFragmentManager, object : ProfileDialog.ClicksListener {
                override fun onClose() {

                }

                override fun onBackPressed() {

                }

            })
        }

        TooltipCompat.setTooltipText(connect, requireContext().getString(R.string.tap_to_connect));

        get_more_time.setOnClickListener {
            goToViewAdsScreen()
        }

        delay({
            if (viewModel.shouldShowAds().not()) {
                get_more_time.visibility = View.GONE
                timer_view.visibility = View.GONE
                time_left_label.visibility = View.GONE
            }
        }, 300)

        test_connection.setOnClickListener {
            WebViewDialog.display(childFragmentManager, WebViewDialog.Companion.WebUrl("https://dnsleaktest.com/", "Connection Test"), null)
        }

        balloon = createBalloon(requireContext()) {
            setArrowSize(10)
            setWidthRatio(0.4f)
            setHeight(50)
            setArrowPosition(0.5f)
            setCornerRadius(4f)
            setAlpha(0.9f)
            setText("Click here to connect")
            setTextColorResource(R.color.black)
            setBackgroundColorResource(R.color.grey)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
    }

    private fun startBlinkingAnimation() {
        if (timer_view.animation == null) {
            val anim: Animation = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 500 //You can manage the blinking time with this parameter
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.INFINITE
            timer_view.startAnimation(anim)
        }
    }

    private fun stopBlinkingAnimation() {
        timer_view.clearAnimation()
    }

    private fun goToViewAdsScreen() {
        findNavController().navigate(R.id.action_HomeFragment_to_AdsFragment)
    }

    private fun observeStates() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.onEach {
                render(it)
            }.launchIn(this)
        }
    }

    private fun initLocationPicker() {
        select_location_card.children.forEach {
            it.setOnClickListener {
                LocationSelectorDialog.display(childFragmentManager, object : LocationSelectorDialog.ClicksListener {
                    override fun onClose() {

                    }

                    override fun onBackPressed() {

                    }

                    override fun onLocationSelected(server: Server) {
                        showDialogOrConnect(server)
                    }
                }, ekoVpnMgrService?.server, viewModel.state.value._serversList)
            }
        }
    }

    private fun showDialogOrConnect(server: Server) {
        if (ekoVpnMgrService?.server == null) {
            connectToServer(server)
        } else {
            requireContext().showAlertDialog({
                disconnectCurrentConnection()
                delay({
                    connectToServer(server)
                }, 1500)
            }, {

            }, getString(R.string.disconnect_notice))
        }
    }

    private fun disconnectCurrentConnection() {
        ekoVpnMgrService?.disconnectCurrentVPN()
    }

    private fun checkForExistingConnection() {
        val currentServer: Server? = ekoVpnMgrService?.server
        Log.d(HomeFragment::class.java.simpleName, currentServer.toString())
        currentServer?.let {
            viewModel.setConnected()
            initCurrentConnectionUI(it.location_)
        }
    }

    private fun connectToServer(server: Server) {
        lifecycleScope.launchWhenResumed {
            if (server is Server.OVPNServer) {
                viewModel.getOVPNProfileForServer(server.ovpnProfileId)?.let {
                    startOrStopOpenVPN(it)
                    viewModel.connectingToServer(server)
                }
            } else if (server is Server.IkeV2Server) {
                viewModel.getIKEv2ProfileForServer(server.profileId)?.let {
                    startOrStopIKEv2(it)
                    viewModel.connectingToServer(server)
                }
            } else if (server is Server.WireGuardServer) {
                if (checkIfWireGuardIsAuthorized()) {
                    getUserVPNPermission.launch(Unit)
                }
                viewModel.connectingToServer(server)
                startOrStopWireGuard(server.tunnelName)
            }
        }
    }

    private fun checkIfWireGuardIsAuthorized(): Boolean {
        val backend = WireGuardInitializer.getBackend()
        if (backend is GoBackend) {
            val intent = GoBackend.VpnService.prepare(requireContext())
            if (intent != null) {
                return true
            }
        }
        return false
    }


    private fun startOrStopWireGuard(tunnelName: String) {
        if (wireGuardService?.getCurrentState() != Tunnel.State.UP) {
            ApplicationClass.getInstance()?.let {
                val intent = Intent(activity, WireGuardService::class.java)
                intent.action = WireGuardService.CONNECT_TO_VPN
                intent.putExtra(WireGuardService.TUNNEL_NAME, tunnelName)
                ContextCompat.startForegroundService(it, intent)
            }
        } else {
            ekoVpnMgrService?.disconnectCurrentVPN()
        }
    }

    private fun startOrStopIKEv2(profile: org.strongswan.android.data.VpnProfile) {
        val intent = Intent(activity, VpnProfileControlActivity::class.java)
        intent.action = VpnProfileControlActivity.START_PROFILE
        intent.putExtra(VpnProfileControlActivity.EXTRA_VPN_PROFILE_ID, profile.uuid.toString())
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        VpnStatus.addStateListener(this)
        viewModel.fetchServersForCurrentProtocol()
        viewModel.fetchTimeLeft()
    }

    private fun initCurrentConnectionUI(location_: Location) {
        activity?.runOnUiThread {
            selected_flag.load("https://www.countryflags.io/${location_.country_code}/flat/64.png")
            selected_location.text = Html.fromHtml(location_.country)
        }
    }

    private fun initLastSelectedUI(server: Server) {
        activity?.runOnUiThread {
            country_flag.load("https://www.countryflags.io/${server.location_.country_code}/flat/64.png")
            location_name.text = Html.fromHtml("${server.location_.city}-${server.location_.country}")
        }
    }

    private fun render(it: HomeState) {
        //Log.d(HomeFragment::class.java.simpleName, "state: $it")
        val timeMilliParser = TimeMilliParser()
        when (it.connectionStatus) {
            HomeState.ConnectionStatus.DISCONNECTED -> {
                connect.setStrokeColorResource(R.color.eko_red_light)
                connection_status_.text = resources.getString(R.string.disconnected_status_)
                connect.isEnabled = true
                connect.isClickable = true
                disconnect.visibility = View.GONE
                connect.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                timer_view.text = timeMilliParser.parseTimeInMilliSeconds(it.timeLeft)
                selected_title.text = resources.getString(R.string.current_location)
                connection_status_.setIconTintResource(R.color.eko_red_light)
                it.currentLocation?.let {
                    initCurrentConnectionUI(it)
                }
                stopCountDownTimerService()
                connect_parent.startRippleAnimation()
                if(it.hasShownBalloonCTA){
                    balloon?.dismiss()
                }else{
                    balloon?.showAlignTop(connect)
                }
            }
            HomeState.ConnectionStatus.CONNECTING -> {
                connection_status_.text = resources.getString(R.string.connecting_status_)
                disconnect.visibility = View.VISIBLE
                connect.visibility = View.GONE
                connect.setStrokeColorResource(R.color.white)
                connection_status_.setIconTintResource(R.color.grey)
                progressBar.visibility = View.VISIBLE
                connect.isEnabled = false
                connect.isClickable = false
                timer_view.text = timeMilliParser.parseTimeInMilliSeconds(it.timeLeft)
                connect_parent.stopRippleAnimation()
            }
            HomeState.ConnectionStatus.CONNECTED -> {
                connection_status_.text = resources.getString(R.string.connected_status_)
                selected_title.text = resources.getString(R.string.selected_location)
                connect.setStrokeColorResource(R.color.connected_green)
                connection_status_.setIconTintResource(R.color.connected_green)
                progressBar.visibility = View.GONE
                disconnect.visibility = View.GONE
                connect.visibility = View.VISIBLE
                connect.isEnabled = true
                timer_view.text = timeMilliParser.parseTimeInMilliSeconds(it.timeLeft)
                connect.isClickable = true
                it.currentConnectionServer?.let {
                    initCurrentConnectionUI(it.location_)
                }
                balloon?.dismiss()
                connect_parent.stopRippleAnimation()
            }
        }

        it.lastUsedServer?.let {
            select_location_card.show()
            initLastSelectedUI(it)
            country_flag.show()
            location_name.show()
        }
    }

    private fun startManagerService(state: HomeState) {
        ApplicationClass.getInstance()?.let {
            val timerServiceIntent = Intent(it, EkoVPNMgrService::class.java)
            if (viewModel.shouldShowAds()) {
                timerServiceIntent.putExtra(EkoVPNMgrService.TIMER_SERVICE_VPN_PROFILE, state.currentConnectionServer)
                timerServiceIntent.putExtra(EkoVPNMgrService.TIMER_SERVICE_TIME_LEFT, state.timeLeft)
                timerServiceIntent.action = EkoVPNMgrService.TIMER_ACTION_START_FREE
            }else{
                timerServiceIntent.putExtra(EkoVPNMgrService.TIMER_SERVICE_VPN_PROFILE, state.currentConnectionServer)
                timerServiceIntent.action = EkoVPNMgrService.TIMER_ACTION_START_PAID
            }
            ContextCompat.startForegroundService(it, timerServiceIntent)
        }

    }

    private fun stopCountDownTimerService() {
        ekoVpnMgrService?.disconnectCurrentVPN()
        ekoVpnMgrService?.stopTimer()
        ekoVpnMgrService?.stopForeground(true)
    }


    private fun startOrStopOpenVPN(profile: VpnProfile) {
        if (VpnStatus.isVPNActive() && profile.uuidString == VpnStatus.getLastConnectedVPNProfile()) {
            val disconnectVPN = Intent(activity, DisconnectVPN::class.java)
            startActivity(disconnectVPN)
        } else {
            startOpenVPN(profile)
        }
    }


    private fun startOpenVPN(profile: VpnProfile) {
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
            startManagerService(viewModel.state.value)
        } else if (level == ConnectionStatus.LEVEL_NOTCONNECTED) {
            viewModel.setDisconnected()
            viewModel.fetchLocationForCurrentIP()
        }
    }

    override fun setConnectedVPN(uuid: String?) {

    }

    override fun failed(error: Throwable) {
        viewModel.state.value.currentConnectionServer?.let {
            viewModel.connectingToServer(it)
        }
        Toast.makeText(context, "There was an error connecting you, we are retrying", Toast.LENGTH_LONG).show()
    }

    override fun onStateChange(status: Tunnel.State) {
        when (status) {
            Tunnel.State.DOWN -> {
                viewModel.setDisconnected()
            }
            Tunnel.State.TOGGLE -> {

            }
            Tunnel.State.UP -> {
                viewModel.setConnected()
                startManagerService(viewModel.state.value)
            }
        }
    }

    override fun stateChanged(state: VpnStateService.State) {
        when (state) {
            VpnStateService.State.DISABLED -> {
                viewModel.setDisconnected()
            }
            VpnStateService.State.CONNECTING -> {

            }
            VpnStateService.State.CONNECTED -> {
                startManagerService(viewModel.state.value)
                viewModel.setConnected()
                if (viewModel.shouldShowAds()) {
                    requireActivity().createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917", getCallback())
                }
            }
            VpnStateService.State.DISCONNECTING -> {

            }
        }
    }

    private fun getCallback(): RewardedAdCallback {
        return object : RewardedAdCallback() {
            override fun onUserEarnedReward(p0: RewardItem) {

            }
        }
    }

    companion object {
        private const val REQUEST_CODE_VPN_PERMISSION = 23491
    }


}