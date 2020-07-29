package com.ekovpn.android.view.main.ads.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekovpn.android.R
import com.ekovpn.android.models.Ad
import com.ekovpn.android.models.Ad.Companion.getIcon
import com.ekovpn.android.models.Ad.Companion.getMinutes
import com.ekovpn.android.models.Ad.Companion.getQuantityDescription
import com.ekovpn.android.utils.SelectionListener
import com.ekovpn.android.utils.ext.recursivelyApplyToChildren
import kotlinx.android.synthetic.main.item_ad.view.*


class AdAdapter(private val selectionListener: SelectionListener<Ad>? = null) : RecyclerView.Adapter<AdAdapter.AdItemViewHolder>() {

    var all: List<Ad> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdItemViewHolder {
        return AdItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ad, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return all.size
    }

    override fun onBindViewHolder(holderMenu: AdItemViewHolder, position: Int) {
        holderMenu.bind(all[position])
    }

    inner class AdItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(advert: Ad) {
            itemView.value.text = advert.getMinutes(itemView.context.resources)
            itemView.quantity.text = advert.getQuantityDescription(itemView.context.resources)
            itemView.quantity.setCompoundDrawablesWithIntrinsicBounds(advert.getIcon(), 0, 0, 0)
            (itemView as ViewGroup).recursivelyApplyToChildren {child->
                child.setOnClickListener {
                    selectionListener?.select(advert)
                }
            }
            itemView.setOnClickListener {
                selectionListener?.select(advert)
            }
        }

    }

}