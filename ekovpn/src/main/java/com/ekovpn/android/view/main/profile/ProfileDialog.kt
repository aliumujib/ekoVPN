package com.ekovpn.android.view.main.profile

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ekovpn.android.R

class ProfileDialog : DialogFragment() {

    private var toolbar: Toolbar? = null
    private var clicksListener: ClicksListener? = null
    private var dialogView: View? = null

    interface ClicksListener {
        fun onClose()
        fun onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_IcsopenvpnNoActionBar_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setWindowAnimations(R.style.Theme_IcsopenvpnNoActionBar_Slide)
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

        toolbar?.title = getString(R.string.select_a_location)


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
