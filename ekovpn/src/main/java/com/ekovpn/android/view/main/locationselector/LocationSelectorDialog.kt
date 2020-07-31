package com.ekovpn.android.view.main.locationselector

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekovpn.android.R
import com.ekovpn.android.models.Server
import com.ekovpn.android.utils.ext.dpToPx
import io.cabriole.decorator.GridDividerDecoration
import io.cabriole.decorator.LinearDividerDecoration
import io.cabriole.decorator.LinearMarginDecoration
import kotlinx.android.synthetic.main.location_picker_dialog.*

class LocationSelectorDialog : DialogFragment() {

    private var servers: List<Server>? = null
    private var toolbar: Toolbar? = null
    private var clicksListener: ClicksListener? = null
    private var dialogView: View? = null

    interface ClicksListener {
        fun onClose()
        fun onBackPressed()
        fun onLocationSelected(server: Server)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_EkoVPN_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setWindowAnimations(R.style.Theme_EkoVPN_NoActionBar_Slide)
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
            dialogView = inflater.inflate(R.layout.location_picker_dialog, container, false)
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

        val locationAdapter = LocationAdapter(object : LocationClickListener{
            override fun onLocationActionClick(model: Server) {
                clicksListener?.onLocationSelected(model)
                dismiss()
            }
        })

        countries.apply {

            addItemDecoration(LinearDividerDecoration.create(
                    color = ContextCompat.getColor(context, R.color.grey),
                    size = resources.dpToPx(1),
                    leftMargin = resources.dpToPx(0),
                    topMargin = resources.dpToPx(0),
                    rightMargin = resources.dpToPx(64),
                    bottomMargin = resources.dpToPx(0),
                    orientation = RecyclerView.VERTICAL
            ))

            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationAdapter
        }

        locationAdapter.submitList(servers)

    }

    companion object {

        private const val TAG: String = "location_picker_dialog"

        fun display(
            fragmentManager: FragmentManager,
            onCloseClicked: ClicksListener? = null,
            servers: List<Server>
        ): LocationSelectorDialog {
            val webViewDialog = LocationSelectorDialog()
            webViewDialog.clicksListener = onCloseClicked
            webViewDialog.servers = servers
            webViewDialog.arguments = Bundle().apply {

            }
            webViewDialog.show(fragmentManager, TAG)
            return webViewDialog
        }
    }
}
