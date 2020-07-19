/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.ads

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autochek.android.ui.commons.adapters.SelectionListener
import com.ekovpn.android.R
import com.ekovpn.android.di.main.ads.AdsModule
import com.ekovpn.android.di.main.ads.DaggerAdsComponent
import com.ekovpn.android.models.Ad
import com.ekovpn.android.utils.ext.dpToPx
import com.ekovpn.android.view.countdowntimer.TimeMilliParser
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import com.ekovpn.android.view.main.ads.adapter.AdAdapter
import io.cabriole.decorator.ColumnProvider
import io.cabriole.decorator.GridMarginDecoration
import kotlinx.android.synthetic.main.fragment_ad.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class AdsFragment : Fragment(), SelectionListener<Ad> {

    @Inject
    lateinit var viewModel: AdsViewModel

    private val adAdapter by lazy {
        AdAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    private fun injectDependencies() {
        DaggerAdsComponent.builder()
                .vPNComponent(vpnComponent(requireActivity()))
                .adsModule(AdsModule(this))
                .build()
                .inject(this)
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

    }

    override fun deselect(item: Ad) {

    }

}