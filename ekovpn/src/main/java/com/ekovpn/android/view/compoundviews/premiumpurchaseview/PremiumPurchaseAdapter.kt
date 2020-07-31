package com.ekovpn.android.view.compoundviews.premiumpurchaseview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekovpn.android.R
import com.ekovpn.android.utils.SelectionListener
import kotlinx.android.synthetic.main.item_billing_sku.view.*


class PremiumPurchaseAdapter(private val selectionListener: SelectionListener<String>? = null) : RecyclerView.Adapter<PremiumPurchaseAdapter.PremiumPurchaseViewHolder>() {

    private var all: List<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PremiumPurchaseViewHolder {
        return PremiumPurchaseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_billing_sku, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return all.size
    }

    fun submitList(data:List<String>){
        all = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holderMenu: PremiumPurchaseViewHolder, position: Int) {
        holderMenu.bind(all[position])
    }

    inner class PremiumPurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(payment: String) {
            itemView.option.text = payment
            itemView.setOnClickListener {
                selectionListener?.select(payment)
            }
        }

    }

}