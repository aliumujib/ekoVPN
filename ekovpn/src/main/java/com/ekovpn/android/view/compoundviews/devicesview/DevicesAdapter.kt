package com.ekovpn.android.view.compoundviews.devicesview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekovpn.android.R
import com.ekovpn.android.models.Device
import com.ekovpn.android.utils.SelectionListener
import kotlinx.android.synthetic.main.item_billing_sku.view.*
import kotlinx.android.synthetic.main.item_devices.view.*


class DevicesAdapter(private val selectionListener: SelectionListener<Device>? = null) : RecyclerView.Adapter<DevicesAdapter.DevicesViewViewHolder>() {

    private var all: MutableList<Device> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewViewHolder {
        return DevicesViewViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_devices, parent, false)
        )
    }

    fun deleteItem(device: Device) {
        all.remove(device)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return all.size
    }

    fun submitList(data:List<Device>){
        all = data.toMutableList()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holderMenu: DevicesViewViewHolder, position: Int) {
        holderMenu.bind(all[position])
    }

    inner class DevicesViewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(device: Device) {
            itemView.phone_name.text =  device.device
            itemView.action_button.setOnClickListener {
                selectionListener?.select(device)
            }
        }

    }

}