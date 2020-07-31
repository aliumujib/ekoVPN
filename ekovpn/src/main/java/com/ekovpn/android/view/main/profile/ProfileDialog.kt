package com.ekovpn.android.view.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.ekovpn.android.R
import com.ekovpn.android.di.main.profile.DaggerProfileComponent
import com.ekovpn.android.di.main.profile.ProfileModule
import com.ekovpn.android.utils.ext.copyToClipBoard
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import kotlinx.android.synthetic.main.profile_dialog.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class ProfileDialog : DialogFragment() {

    private var toolbar: Toolbar? = null
    private var clicksListener: ClicksListener? = null
    private var dialogView: View? = null

    @Inject
    lateinit var viewModel: ProfileViewModel

    interface ClicksListener {
        fun onClose()
        fun onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_EkoVPN_FullScreenDialog)
        injectDependencies()
    }

    private fun injectDependencies() {
        DaggerProfileComponent
                .builder()
                .vPNComponent(vpnComponent(requireActivity()))
                .profileModule(ProfileModule(this))
                .build()
                .inject(this)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setWindowAnimations(R.style.Theme_EkoVPN_Slide)
            disableBackClick()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        if (dialogView == null) {
            dialogView = inflater.inflate(R.layout.profile_dialog, container, false)
        }

        toolbar = dialogView?.findViewById(R.id.toolbar)

        disableBackClick()

        return dialogView
    }


    private fun disableBackClick() {
        dialogView?.isFocusableInTouchMode = true
        dialogView?.requestFocus()
        dialogView?.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    clicksListener?.onBackPressed()
                    dismiss()
                }
            }
            return@setOnKeyListener false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar?.setNavigationOnClickListener { v ->
            clicksListener?.onClose()
            dismiss()
        }

        toolbar?.title = getString(R.string.profile)


        initViews()

        viewModel.state.onEach {
            handleStates(it)
        }.launchIn(lifecycleScope)

        //premium_options.submitPremiumPurchaseList(listOf("Unlimited for 1 Month\t\t $5.99", "Unlimited for 1 Year\t\t $49.99"))
    }

    private fun shareText(text: String?) {
        val mimeType = "text/plain"
        val title = "Eko VPN"
        val shareIntent: Intent = ShareCompat.IntentBuilder.from(requireActivity())
                .setType(mimeType)
                .setText(text)
                .intent
        startActivity(shareIntent)
    }

    private fun initViews() {
        referral_code.setActionTitle(getString(R.string.referral_title))
        account_number.setActionButtonClickListener(View.OnClickListener {
            viewModel.state.value.user?.account_id?.let {
                requireContext().copyToClipBoard(it)
                Toast.makeText(requireContext(), getString(R.string.account_number_copied), Toast.LENGTH_LONG).show()
            }
        })

        referral_code.setActionButtonClickListener(View.OnClickListener {
            viewModel.state.value.user?.referral_id?.let {
                shareText(it)
            }
        })
    }

    private fun handleStates(profileState: ProfileState) {
        account_number.setActionSubTitle(getString(R.string.account_number_subtitle, profileState.user?.account_id))
        account_type.setActionSubTitle(getString(R.string.account_type_subtitle, profileState.user?.account_type))
        renewal_date.setActionSubTitle(getString(R.string.renewal_date_subtitle, profileState.user?.renewal_at))
        referral_code.setActionSubTitle(getString(R.string.referral_sub_title, profileState.user?.referral_id))
    }

    companion object {

        private const val TAG: String = "profile_dialog"

        fun display(
                fragmentManager: FragmentManager,
                onCloseClicked: ClicksListener? = null): ProfileDialog {
            val webViewDialog = ProfileDialog()
            webViewDialog.clicksListener = onCloseClicked
            webViewDialog.arguments = Bundle().apply {

            }
            webViewDialog.show(fragmentManager, TAG)
            return webViewDialog
        }
    }
}
