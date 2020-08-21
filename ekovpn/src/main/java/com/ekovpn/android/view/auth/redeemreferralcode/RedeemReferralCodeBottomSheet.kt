package com.ekovpn.android.view.auth.redeemreferralcode

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.ekovpn.android.R
import com.ekovpn.android.view.base.BaseRoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_account_recovery_bottom_sheet.confirm_btn
import kotlinx.android.synthetic.main.layout_referral_bottom_sheet.*

class RedeemReferralCodeBottomSheet(
    private val confirmationListener: (refferalCode:String) -> Unit
) : BaseRoundedBottomSheetDialogFragment(){


    override fun getLayoutRes(): Int = R.layout.layout_referral_bottom_sheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        referral_code_input.requestFocus()

        confirm_btn.setOnClickListener {
            if(referral_code_input.text.toString().isNotEmpty()){
                confirmationListener.invoke(referral_code_input.text.toString())
                dismiss()
            }else{
                Toast.makeText(context, "Please enter your referral code", Toast.LENGTH_LONG).show()
            }
        }

    }





}