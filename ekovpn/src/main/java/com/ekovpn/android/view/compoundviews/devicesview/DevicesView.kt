package com.ekovpn.android.view.compoundviews.devicesview

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.ekovpn.android.R
import com.ekovpn.android.models.Device
import com.ekovpn.android.utils.SelectionListener
import com.ekovpn.android.utils.ext.dpToPx
import io.cabriole.decorator.LinearMarginDecoration
import kotlinx.android.synthetic.main.devices_view.view.*
import kotlinx.android.synthetic.main.premium_purchase_view.view.*
import kotlinx.android.synthetic.main.profile_action_view.view.divider
import kotlinx.coroutines.withContext


class DevicesView : LinearLayout {

    private val listeners = mutableListOf<DeviceOpListener>()

    private val devicesAdapter by lazy {
        DevicesAdapter(object : SelectionListener<Device> {
            override fun select(item: Device) {
                listeners.forEach {
                    it.onDeleteClickListener(item)
                }
            }

            override fun deselect(item: Device) {

            }
        })
    }


    private var showDivider = false
    private var view: View? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.devices_view, this, true)

        attrs?.let {
            with(context.obtainStyledAttributes(attrs, R.styleable.DevicesView)) {
                showDivider = getBoolean(
                        R.styleable.DevicesView_dv_show_divider,
                        true
                )

                recycle()
            }
        }
        initRecyclerview()
    }

    private fun initRecyclerview() {
        devices_rv.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = devicesAdapter
        }
    }

    fun addDeviceOpListener(listener: DeviceOpListener) {
        listeners.add(listener)
    }

    fun removeDeviceOpListener(listener: DeviceOpListener) {
        listeners.remove(listener)
    }

    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    fun submitDeviceList(list: List<Device>) {
        devicesAdapter.submitList(list)
    }

    interface DeviceOpListener {
        fun onDeleteClickListener(device: Device)
    }

}