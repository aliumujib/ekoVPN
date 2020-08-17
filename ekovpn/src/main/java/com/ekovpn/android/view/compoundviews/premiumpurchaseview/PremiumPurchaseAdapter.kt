package com.ekovpn.android.view.compoundviews.premiumpurchaseview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.ekovpn.android.R
import com.ekovpn.android.utils.SelectionListener
import kotlinx.android.synthetic.main.item_billing_sku.view.*


class PremiumPurchaseAdapter(private val selectionListener: SelectionListener<SkuDetails>? = null) : RecyclerView.Adapter<PremiumPurchaseAdapter.PremiumPurchaseViewHolder>() {

    private var all: List<SkuDetails> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PremiumPurchaseViewHolder {
        return PremiumPurchaseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_billing_sku, parent, false)
        )
    }

    fun getItemAtPosition(position: Int): SkuDetails? {
        return if(position <= all.lastIndex){
            all[position]
        }else{
            null
        }
    }

    override fun getItemCount(): Int {
        return all.size
    }

    fun submitList(data:List<SkuDetails>){
        all = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holderMenu: PremiumPurchaseViewHolder, position: Int) {
        holderMenu.bind(all[position])
    }

    inner class PremiumPurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(payment: SkuDetails) {
            itemView.option.text =  "${payment.title.split("(").firstOrNull()} - ${payment.price}"
            itemView.setOnClickListener {
                selectionListener?.select(payment)
            }
        }

    }

}