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
import com.ekovpn.android.models.Section
import com.ekovpn.android.models.Server
import com.ekovpn.android.utils.ext.dpToPx
import io.cabriole.decorator.LinearDividerDecoration
import kotlinx.android.synthetic.main.location_picker_dialog.*

class LocationSelectorDialog : DialogFragment() {

    private var servers: List<Server>? = null
    private var selectedServer: Server? = null
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

        val locationAdapter = LocationAdapter(object : LocationClickListener {
            override fun onLocationActionClick(model: Server) {
                clicksListener?.onLocationSelected(model)
                dismiss()
            }
        }, mutableListOf(), selectedServer)

        countries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationAdapter
        }

        val countriesList = mutableListOf<Section<Server>>()

        servers?.forEach { server ->
            var section = countriesList.find {
                it.location.country_code == server.location_.country_code
            }

            if (section == null) {
                section = Section(server.location_, mutableListOf(server))
                countriesList.add(section)
            } else {
                section.data.add(server)
            }
        }

        locationAdapter.submitList(countriesList)
        locationAdapter.collapseAllSections()
    }

    companion object {

        private const val TAG: String = "location_picker_dialog"

        fun display(
                fragmentManager: FragmentManager,
                onCloseClicked: ClicksListener? = null,
                selectedServer: Server? = null,
                servers: List<Server>
        ): LocationSelectorDialog {
            val webViewDialog = LocationSelectorDialog()
            webViewDialog.clicksListener = onCloseClicked
            webViewDialog.servers = servers
            webViewDialog.selectedServer = selectedServer
            webViewDialog.arguments = Bundle().apply {

            }
            webViewDialog.show(fragmentManager, TAG)
            return webViewDialog
        }
    }
}
