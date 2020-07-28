package com.ekovpn.android.view.premiumpurchaseview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekovpn.android.R
import com.ekovpn.android.utils.SelectionListener
import com.ekovpn.android.utils.ext.dpToPx
import com.ekovpn.android.view.main.ads.adapter.AdAdapter
import io.cabriole.decorator.ColumnProvider
import io.cabriole.decorator.GridMarginDecoration
import io.cabriole.decorator.LinearMarginDecoration
import kotlinx.android.synthetic.main.fragment_ad.*
import kotlinx.android.synthetic.main.premium_purchase_view.view.*
import kotlinx.android.synthetic.main.profile_action_view.view.*
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
    }

    private fun initRecyclerview() {
        premium_options_rv.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(
                    LinearMarginDecoration(
                            topMargin = resources.dpToPx(8),
                            bottomMargin =  resources.dpToPx(8),
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