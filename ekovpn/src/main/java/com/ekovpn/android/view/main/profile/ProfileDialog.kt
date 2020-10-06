package com.ekovpn.android.view.main.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.ekovpn.android.models.Device
import com.ekovpn.android.utils.ext.*
import com.ekovpn.android.view.auth.AuthActivity
import com.ekovpn.android.view.compoundviews.devicesview.DevicesView
import com.ekovpn.android.view.compoundviews.premiumpurchaseview.PremiumPurchaseView
import com.ekovpn.android.view.main.VpnActivity.Companion.vpnComponent
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.profile_dialog.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class ProfileDialog : DialogFragment(), PremiumPurchaseView.PurchaseProcessListener, DevicesView.DeviceOpListener {

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
        initAdControls()


        viewModel.state.onEach {
            handleStates(it)
        }.launchIn(lifecycleScope)

    }

    private fun shareText(text: String?) {
        val mimeType = "text/plain"
        val title = "Eko VPN"
        val shareIntent: Intent = ShareCompat.IntentBuilder.from(requireActivity())
                .setType(mimeType)
                .setText(resources.getString(R.string.share_referral, text))
                .intent
        startActivity(shareIntent)
    }

    private fun initViews() {
        referral_code.setActionTitle(getString(R.string.referral_title))
        account_number.setActionButtonClickListener(View.OnClickListener {
            copyAccountNumberToClipBoard()
        })
        account_number.setOnClickListener {
            copyAccountNumberToClipBoard()
        }
        account_type.setActionButtonClickListener(View.OnClickListener {
            premium_options.triggerItemPurchase(0)
        })
        (account_number as ViewGroup).recursivelyApplyToChildren {
            it.setOnClickListener {
                copyAccountNumberToClipBoard()
                if (viewModel.shouldShowAds()) {
                    requireActivity().createAndLoadInterstitialAd(resources.getString(R.string.interstitial_ad_after_action_))
                }
            }
        }

        logout.setOnClickListener {
            viewModel.logOut()
        }
        referral_code.setActionButtonClickListener(View.OnClickListener {
            viewModel.fetchReferralId()?.let {
                shareText(it)
            }
        })

    }

    private fun copyAccountNumberToClipBoard() {
        viewModel.fetchAccountId()?.let {
            requireContext().copyToClipBoard(it)
            Toast.makeText(requireContext(), getString(R.string.account_number_copied), Toast.LENGTH_LONG).show()
        }
    }

    private fun handleStates(profileState: ProfileState) {
        Log.d(ProfileDialog::class.java.simpleName, profileState.toString())
        if(profileState.error == null){
            if (handlePossibilityOfLogout(profileState)) return
            profileState.user?.let {
                account_number.setActionSubTitle(getString(R.string.account_number_subtitle,insertPeriodically(it.account_id, " ", 4) ))
                devices_view.submitDeviceList(it.devices)
            }
            account_type.setActionSubTitle(getString(R.string.account_type_subtitle, profileState.user?.account_type?.type?.capitalize()))

            renewal_date.setActionSubTitle(getString(R.string.renewal_date_subtitle, profileState.user?.renewal_at))
            referral_code.setActionSubTitle(getString(R.string.referral_sub_title, profileState.user?.referral_id))
        }else{
            Toast.makeText(requireContext(), getString(R.string.error_performing_request), Toast.LENGTH_LONG).show()
        }
    }

    private fun handlePossibilityOfLogout(profileState: ProfileState): Boolean {
        if (profileState.isLoggedOut) {
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
            return true
        }
        return false
    }

    private fun initAdControls() {
      delay({
          if (viewModel.shouldShowAds()){
              val adRequest = AdRequest.Builder().build()
              adView.loadAd(adRequest)
          }
      }, 300)

    }

    override fun onResume() {
        super.onResume()
        premium_options.addListener(this)
        devices_view.addDeviceOpListener(this)
    }

    override fun onStop() {
        super.onStop()
        premium_options.removeListener(this)
        devices_view.removeDeviceOpListener(this)
    }

    companion object {

        private const val TAG: String = "profile_dialog"

        fun display(
                fragmentManager: FragmentManager,
                onCloseClicked: ClicksListener? = null): ProfileDialog {
            val profileDialog = ProfileDialog()
            profileDialog.clicksListener = onCloseClicked
            profileDialog.arguments = Bundle().apply {

            }
            profileDialog.show(fragmentManager, TAG)
            return profileDialog
        }
    }

    override fun handleSuccessfulSubscription(orderId: String, purchaseToken:String) {
        viewModel.updateUserWithOrderData(orderId, purchaseToken)
    }

    override fun handleUserCancellation() {
        Toast.makeText(context, "You cancelled, we hope you will reconsider :(", Toast.LENGTH_LONG).show()
    }

    override fun handleOtherError(error: Int) {
        Toast.makeText(context, "Failed to complete purchase, any debits will be automatically refunded", Toast.LENGTH_LONG).show()
    }

    override fun onDeleteClickListener(device: Device) {
        requireContext().showAlertDialog({
            viewModel.deleteDevice(device, requireContext().getDeviceId())
        }, {

        }, "Are you sure you want to remove this device?")
    }

}
