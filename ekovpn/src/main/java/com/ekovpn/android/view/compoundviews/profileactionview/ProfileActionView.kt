package com.ekovpn.android.view.compoundviews.profileactionview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.ekovpn.android.R
import kotlinx.android.synthetic.main.profile_action_view.view.*

class ProfileActionView : LinearLayout {

    private var title: String? = null
    private var subTitle: String? = null
    private var actionText: String? = null
    private var icon: Drawable? = null
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
        view = inflater.inflate(R.layout.profile_action_view, this, true)

        attrs?.let {
            with(context.obtainStyledAttributes(attrs, R.styleable.ProfileActionView)) {
                val titleId = getResourceId(
                        R.styleable.ProfileActionView_action_title,
                        View.NO_ID
                )
                val subTitleId = getResourceId(
                        R.styleable.ProfileActionView_action_subtitle,
                        View.NO_ID
                )
                val actionTextId = getResourceId(
                        R.styleable.ProfileActionView_action_button_text,
                        View.NO_ID
                )
                showDivider = getBoolean(
                        R.styleable.ProfileActionView_show_divider,
                        true
                )
                title = if (titleId == View.NO_ID) {
                    getString(
                            R.styleable.ProfileActionView_action_title
                    )
                } else {
                    resources.getString(titleId)
                }

                subTitle = if (subTitleId == View.NO_ID) {
                    getString(
                            R.styleable.ProfileActionView_action_subtitle
                    )
                } else {
                    resources.getString(subTitleId)
                }


                actionText = if (actionTextId == View.NO_ID) {
                    getString(
                            R.styleable.ProfileActionView_action_button_text
                    )
                } else {
                    resources.getString(actionTextId)
                }

                icon = getDrawable(
                        R.styleable.ProfileActionView_action_icon
                )

                subTitle?.let {
                    setActionSubTitle(it)
                }

                title?.let {
                    setActionTitle(it)
                }

                actionText?.let {
                    setActionButtonText(it)
                }

                icon?.let {
                    setIcon(it)
                }

                showDivider(showDivider)

                recycle()
            }
        }
    }

    private fun showDivider(show:Boolean) {
        if(show){
            divider.visibility = View.VISIBLE
        }else{
            divider.visibility = View.GONE
        }
    }

    private fun setIcon(drawable: Drawable) {
        iconView.setImageDrawable(drawable)
    }

     fun setActionTitle(title: String) {
        label.text = Html.fromHtml(title)
    }

     fun setActionSubTitle(subtitle: String) {
        description.text = Html.fromHtml(subtitle)
    }

    private fun setActionButtonText(title: String) {
        action_button.text = Html.fromHtml(title)
    }

    fun setActionButtonClickListener(listener: OnClickListener) {
        action_button.setOnClickListener(listener)
    }

}