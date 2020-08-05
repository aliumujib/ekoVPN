package com.ekovpn.android.view.compoundviews.premiumpurchaseview

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
import com.ekovpn.android.utils.SelectionListener
import com.ekovpn.android.utils.ext.dpToPx
import io.cabriole.decorator.LinearMarginDecoration
import kotlinx.android.synthetic.main.premium_purchase_view.view.*
import kotlinx.android.synthetic.main.profile_action_view.view.divider
import kotlinx.coroutines.withContext


class PremiumPurchaseView : LinearLayout {

    private val listeners = mutableListOf<PurchaseProcessListener>()

    private val premiumPurchaseAdapter by lazy {
        PremiumPurchaseAdapter(object : SelectionListener<SkuDetails> {
            override fun select(item: SkuDetails) {
                launchBillingFlow(item)
            }

            override fun deselect(item: SkuDetails) {

            }
        })
    }

    private val purchasesUpdateListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    listeners.forEach {
                        it.handleUserCancellation()
                    }
                } else {
                    // Handle any other error codes.
                }
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

    private fun launchBillingFlow(skuDetails: SkuDetails) {
        val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        getActivity()?.let {
            val responseCode = billingClient.launchBillingFlow(it, billingFlowParams).responseCode
        }
    }

    private fun connectToBilling() {
        Log.d(PremiumPurchaseView::class.simpleName, "connecting to Billing")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    querySkuDetails()
                } else {
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
                skuDetailsList?.let {
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


    private fun handlePurchase(purchase: Purchase) {
        val acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                listeners.forEach {
                    it.handleSuccessfulSubscription(purchase.orderId)
                }
            }else{
                listeners.forEach {
                    it.handleOtherError(billingResult.responseCode)
                }
            }
        }
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener)
            }
        }
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

    fun addListener(listener: PurchaseProcessListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PurchaseProcessListener) {
        listeners.remove(listener)
    }

    private fun submitPremiumPurchaseList(list: List<SkuDetails>) {
        premiumPurchaseAdapter.submitList(list)
    }

    private fun showDivider(show: Boolean) {
        if (show) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.GONE
        }
    }

    interface PurchaseProcessListener {
        fun handleSuccessfulSubscription(orderId: String)
        fun handleUserCancellation()
        fun handleOtherError(error: Int)
    }


}