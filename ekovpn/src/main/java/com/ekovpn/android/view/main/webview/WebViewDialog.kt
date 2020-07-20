package com.ekovpn.android.view.main.webview

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ekovpn.android.R
import com.ekovpn.android.utils.ext.hide
import com.ekovpn.android.utils.ext.show
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.web_view_dialog.*
import kotlinx.android.synthetic.main.web_view_dialog.view.*

class WebViewDialog : DialogFragment() {

    private var toolbar: Toolbar? = null
    private var onCloseClickListener: OnCloseClickListener? = null
    private var dialogView: View? = null

    interface OnCloseClickListener {
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
            dialogView = inflater.inflate(R.layout.web_view_dialog, container, false)
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
                    onCloseClickListener?.onBackPressed()
                    dismiss()
                }
            }
            return@setOnKeyListener false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar?.setNavigationOnClickListener { v ->
            onCloseClickListener?.onClose()
            dismiss()
        }

        val url = arguments?.getParcelable<WebUrl>(URL)
        Log.d(WebViewDialog::class.java.simpleName, "$url trying to open")
        toolbar?.title = url?.title


        url?.let {
            //view.webview.settings.javaScriptEnabled = true
            view.webview.settings.domStorageEnabled = true
            view.webview.loadUrl(url.url)
            view.webview.webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progress_circular?.show()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    progress_circular?.hide()
                    url?.let {

                    }
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    onCloseClickListener?.onClose()
                }
            }
        }
    }

    companion object {

        private const val TAG: String = "web_view_dialog"
        private const val URL: String = "_URL_TO_OPEN"

        @Parcelize
        data class WebUrl(val url: String, val title: String) : Parcelable

        fun display(
                fragmentManager: FragmentManager,
                url: WebUrl,
                onCloseClicked: OnCloseClickListener? = null
        ): WebViewDialog {
            val webViewDialog = WebViewDialog()
            webViewDialog.onCloseClickListener = onCloseClicked
            webViewDialog.arguments = Bundle().apply {
                putParcelable(URL, url)
            }
            webViewDialog.show(fragmentManager, TAG)
            return webViewDialog
        }
    }
}
