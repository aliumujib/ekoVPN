package com.ekovpn.android.view.auth.accountnumberbottomsheeet

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.ekovpn.android.R
import com.ekovpn.android.view.base.BaseRoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_account_recovery_bottom_sheet.*

class AccountRecoveryBottomSheet(
    private val confirmationListener: (accountNumber:String) -> Unit
) : BaseRoundedBottomSheetDialogFragment(){


    override fun getLayoutRes(): Int = R.layout.layout_account_recovery_bottom_sheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        order_number_textfield.requestFocus()

        confirm_btn.setOnClickListener {
            if(order_number_input.text.toString().isNotEmpty()){
                confirmationListener.invoke(order_number_input.text.toString())
                dismiss()
            }else{
                Toast.makeText(context, "Please enter your order number", Toast.LENGTH_LONG).show()
            }
        }

    }





}