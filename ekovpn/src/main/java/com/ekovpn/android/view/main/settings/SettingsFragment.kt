/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.main.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ekovpn.android.BuildConfig
import com.ekovpn.android.R
import com.ekovpn.android.models.Protocol
import com.ekovpn.android.di.main.settings.DaggerSettingsComponent
import com.ekovpn.android.di.main.settings.SettingsModule
import com.ekovpn.android.utils.ext.createAndLoadInterstitialAd
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import com.ekovpn.android.view.main.webview.WebViewDialog
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class SettingsFragment : Fragment() {

    @Inject
    lateinit var viewModel: SettingsViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }


    private val checkChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        when (checkedId) {
            R.id.tcp -> {
                viewModel.selectProtocol(Protocol.TCP)
            }
            R.id.udp -> {
                viewModel.selectProtocol(Protocol.UDP)
            }
            R.id.ikev2 -> {
                viewModel.selectProtocol(Protocol.IKEv2)
            }
            else -> {
                viewModel.selectProtocol(Protocol.WIREGUARD)
            }
        }
        if (viewModel.shouldShowAds()) {
            requireActivity().createAndLoadInterstitialAd(resources.getString(R.string.interstitial_ad_after_action_))
        }
        Toast.makeText(requireContext(), getString(R.string.new_protocol_selected), Toast.LENGTH_LONG).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state
                .onEach {
                    delay(1000)
                    render(it)
                }
                .launchIn(lifecycleScope)


        tcp.text = Html.fromHtml(getString(R.string.tcp_open_vpn_title_explanantion))
        udp.text = Html.fromHtml(getString(R.string.udp_open_vpn_title_explanantion))
        ikev2.text = Html.fromHtml(getString(R.string.ikev2_title_explanantion))
        wire_guard.text = Html.fromHtml(getString(R.string.wireguard_title_explanantion))

        protocol_group.setOnCheckedChangeListener(checkChangeListener)

        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.Q){
            ikev2.isEnabled = false
        }

        star_ratings.children.forEach {
            it.setOnClickListener {
                openPlayStore()
            }
        }

        twitter.children.forEach {
            it.setOnClickListener {
                openTwitter()
            }
        }

        facebook.children.forEach {
            it.setOnClickListener {
                openFacebook()
            }
        }

        instagram.children.forEach {
            it.setOnClickListener {
                openInstagram()
            }
        }

        contact_support.setOnClickListener {
            WebViewDialog.display(childFragmentManager, WebViewDialog.Companion.WebUrl("https://www.ekovpn.com/support", "Contact Support"), null)
        }

        privacy.setOnClickListener {
            WebViewDialog.display(childFragmentManager, WebViewDialog.Companion.WebUrl("https://www.ekovpn.com/privacy-policy", "Privacy Policy"), null)
        }

        help.setOnClickListener {
            WebViewDialog.display(childFragmentManager, WebViewDialog.Companion.WebUrl("https://www.ekovpn.com/what-is-a-vpn", "Help"), null)
        }

        app_version.text = resources.getString(R.string.app_version, BuildConfig.VERSION_NAME)

    }

    private fun openInstagram() {
        val uri = Uri.parse("http://instagram.com/_u/eko.vpn")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)
        likeIng.setPackage("com.instagram.android")
        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/eko.vpn")))
        }
    }

    private fun openFacebook() {
        val intentApp = Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/@ekovpn"))
        val intentBrowser = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.facebook.com/ekovpn/community"))
        try {
            this.startActivity(intentApp)
        } catch (ex: ActivityNotFoundException) {
            this.startActivity(intentBrowser)
        }
    }

    private fun openTwitter() {
        var intent: Intent
        try {
            // get the Twitter app if possible
            requireActivity().packageManager.getPackageInfo("com.twitter.android", 0)
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=ekovpn"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } catch (e: Exception) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/ekovpn"))
        }
        this.startActivity(intent)
    }

    private fun openPlayStore() {
        val appPackageName: String = requireActivity().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    private fun render(it: SettingsState) {
        selectCurrentProtocol(it)
    }

    private fun selectCurrentProtocol(it: SettingsState) {
        protocol_group.setOnCheckedChangeListener(null)
        when (it.selectedProtocol) {
            Protocol.TCP -> {
                protocol_group.check(R.id.tcp)
            }
            Protocol.UDP -> {
                protocol_group.check(R.id.udp)
            }
            Protocol.IKEv2 -> {
                protocol_group.check(R.id.ikev2)
            }
            Protocol.WIREGUARD -> {
                protocol_group.check(R.id.wire_guard)
            }
        }
        protocol_group.setOnCheckedChangeListener(checkChangeListener)
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