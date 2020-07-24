/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.ads

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekovpn.android.ApplicationClass
import com.ekovpn.android.R
import com.ekovpn.android.di.main.ads.AdsModule
import com.ekovpn.android.di.main.ads.DaggerAdsComponent
import com.ekovpn.android.models.Ad
import com.ekovpn.android.service.EkoVPNMgrService
import com.ekovpn.android.utils.ext.dpToPx
import com.ekovpn.android.view.countdowntimer.TimeMilliParser
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import com.ekovpn.android.view.main.ads.adapter.AdAdapter
import com.ekovpn.android.view.main.ads.adapter.SelectionListener
import io.cabriole.decorator.ColumnProvider
import io.cabriole.decorator.GridMarginDecoration
import kotlinx.android.synthetic.main.fragment_ad.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class AdsFragment : Fragment(), SelectionListener<Ad>, EkoVPNMgrService.TimeLeftListener {

    @Inject
    lateinit var viewModel: AdsViewModel

    private var ekoVpnMgrService: EkoVPNMgrService? = null

    private val mTimerServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            ekoVpnMgrService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            ekoVpnMgrService = (service as EkoVPNMgrService.VPNTimerLocalBinder).getService()
            ekoVpnMgrService?.registerListener(this@AdsFragment)
        }
    }


    private val viewAdsCall =
            registerForActivityResult(ViewAdsContract()) { result ->
                if (result != null) {
                    addMoreTime(result)
                } else {
                    Toast.makeText(requireContext(), "You cancelled the ad and won't get a reward", Toast.LENGTH_LONG).show()
                }
            }

    private fun addMoreTime(result: Ad) {
        if (ekoVpnMgrService?.server == null) {
            Log.d(AdsFragment::class.java.simpleName, "Added via viewmodel $ekoVpnMgrService")
            viewModel.saveAddedTime(result.timeAddition)
        } else {
            ApplicationClass.getInstance()?.let {
                Log.d(AdsFragment::class.java.simpleName, "Added via service $ekoVpnMgrService")
                val serviceIntent = Intent(it, ekoVpnMgrService!!::class.java)
                serviceIntent.action = EkoVPNMgrService.TIMER_SERVICE_INCREASE_TIME_LEFT_ACTION
                serviceIntent.putExtra(EkoVPNMgrService.TIMER_SERVICE_INCREMENT, result.timeAddition)
                it.startService(serviceIntent)
            }
        }
    }


    private val adAdapter by lazy {
        AdAdapter(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectDependencies()
        bindToTimerService()
    }

    private fun bindToTimerService() {
        ApplicationClass.getInstance()?.let {
            it.bindService(Intent(it, EkoVPNMgrService::class.java), mTimerServiceConnection, Service.BIND_AUTO_CREATE)
        }
    }

    private fun injectDependencies() {
        DaggerAdsComponent.builder()
                .vPNComponent(vpnComponent(requireActivity()))
                .adsModule(AdsModule(this))
                .build()
                .inject(this)
    }


    override fun onStart() {
        super.onStart()
        ekoVpnMgrService?.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        ekoVpnMgrService?.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ApplicationClass.getInstance()?.let {
            it.unbindService(mTimerServiceConnection)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ads_types.apply {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(GridMarginDecoration(
                    margin = resources.dpToPx(16),
                    columnProvider = object : ColumnProvider {
                        override fun getNumberOfColumns(): Int = 2
                    },
                    orientation = RecyclerView.VERTICAL
            ))
            adapter = adAdapter
        }

        viewModel.state.onEach {
            render(it)
        }.launchIn(lifecycleScope)

    }

    private fun render(it: AdsState) {
        Log.d(AdsFragment::class.java.simpleName, "$it")
        adAdapter.all = it.ads
        adAdapter.notifyDataSetChanged()
        timer_view.text = TimeMilliParser().parseTimeInMilliSeconds(it.timeLeft)
    }

    override fun select(item: Ad) {
        Log.d(AdsFragment::class.java.simpleName, "$item")
        viewAdsCall.launch(item)
    }

    override fun deselect(item: Ad) {

    }

    override fun onTimeUpdate(timeLeftMillis: Long, timeLeftFormatted: String) {
        timer_view.text = timeLeftFormatted
    }


}