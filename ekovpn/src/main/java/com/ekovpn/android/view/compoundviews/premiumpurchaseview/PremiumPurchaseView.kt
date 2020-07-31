package com.ekovpn.android.view.compoundviews.premiumpurchaseview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.ekovpn.android.R
import com.ekovpn.android.utils.SelectionListener
import com.ekovpn.android.utils.ext.dpToPx
import io.cabriole.decorator.LinearMarginDecoration
import kotlinx.android.synthetic.main.premium_purchase_view.view.*
import kotlinx.android.synthetic.main.profile_action_view.view.divider


class PremiumPurchaseView : LinearLayout {

    private val premiumPurchaseAdapter by lazy {
        PremiumPurchaseAdapter(object : SelectionListener<String> {
            override fun select(item: String) {

            }

            override fun deselect(item: String) {

            }
        })
    }

    private val purchasesUpdateListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
            }

    private var billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdateListener)
            .enablePendingPurchases()
            .build()

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
        view = inflater.inflate(R.layout.premium_purchase_view, this, true)

        attrs?.let {
            with(context.obtainStyledAttributes(attrs, R.styleable.PremiumPurchaseView)) {
                showDivider = getBoolean(
                        R.styleable.PremiumPurchaseView_bold_title,
                        true
                )

                showDivider(showDivider)
                recycle()
            }
        }
        initRecyclerview()
        connectToBilling()
    }

    private fun connectToBilling() {
        Log.d(PremiumPurchaseView::class.simpleName, "connecting to Billing")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    querySkuDetails()
                }else{
                    Log.d(PremiumPurchaseView::class.simpleName, "${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                connectToBilling()
                Log.d(PremiumPurchaseView::class.simpleName, "Billing disconnected")
            }
        })
    }

    fun querySkuDetails() {
        Log.d(PremiumPurchaseView::class.simpleName, "querying skus")
        val skuList = ArrayList<String>()
        skuList.add("unlimited_for_one_month")
        skuList.add("unlimited_for_one_year")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(PremiumPurchaseView::class.simpleName, "found ${skuDetailsList?.size} products")
                val products = skuDetailsList?.map {
                    it.title
                }
                products?.let {
                    submitPremiumPurchaseList(it)
                }
            } else {
                Log.d(PremiumPurchaseView::class.simpleName, "${billingResult.responseCode}")
            }
        }
    }

    private fun initRecyclerview() {
        premium_options_rv.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(
                    LinearMarginDecoration(
                            topMargin = resources.dpToPx(8),
                            bottomMargin = resources.dpToPx(8),
                            orientation = VERTICAL
                    )
            )
            adapter = premiumPurchaseAdapter
        }
    }

    fun submitPremiumPurchaseList(list: List<String>) {
        premiumPurchaseAdapter.submitList(list)
    }

    private fun showDivider(show: Boolean) {
        if (show) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.GONE
        }
    }


}