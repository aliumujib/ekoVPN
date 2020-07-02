package com.ekovpn.android.view.main.locationselector

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.ekovpn.android.R
import com.ekovpn.android.models.Server
import kotlinx.android.synthetic.main.location_item.view.*
import org.jetbrains.anko.childrenRecursiveSequence

class LocationAdapter constructor(private val locationClickListener: LocationClickListener) :
    ListAdapter<Server, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent, false)
        return MenuActionViewHolder(binding, locationClickListener)
    }

    fun isEmpty() = super.getItemCount() == 0


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MenuActionViewHolder).bind(getItem(position))
    }


    class MenuActionViewHolder(
        private val view: View,
        private val locationClickListener: LocationClickListener
    ) :
        RecyclerView.ViewHolder(view) {

        fun bind(model: Server) {
            view.childrenRecursiveSequence().forEach {
                it.setOnClickListener {
                    locationClickListener.onLocationActionClick(model)
                }
            }
            view.country_flag.load("https://www.countryflags.io/${model.location_.country_code}/flat/64.png")
            view.country_name.text = Html.fromHtml("${model.location_.city}-${model.location_.country}")
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<Server>() {
        override fun areItemsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem.id_ == newItem.id_
        }

        override fun areContentsTheSame(
            oldItem: Server,
            newItem: Server
        ): Boolean {
            return oldItem == newItem
        }
    }
}