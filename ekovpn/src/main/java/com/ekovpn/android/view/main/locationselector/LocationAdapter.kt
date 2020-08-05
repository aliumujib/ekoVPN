package com.ekovpn.android.view.main.locationselector

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.api.load
import com.ekovpn.android.R
import com.ekovpn.android.models.Location
import com.ekovpn.android.models.Section
import com.ekovpn.android.models.Server
import com.ekovpn.android.utils.ext.setRightDrawable
import com.ekovpn.android.view.sectionedadapter.SectionedRecyclerViewAdapter
import com.ekovpn.android.view.sectionedadapter.SectionedViewHolder
import kotlinx.android.synthetic.main.item_location_header.view.*
import kotlinx.android.synthetic.main.item_location_subheader.view.*
import org.jetbrains.anko.childrenRecursiveSequence

class LocationAdapter constructor(private val locationClickListener: LocationClickListener,
                                  private var countriesList: List<Section<Server>>,
                                  private val currentServer: Server? = null) :
        SectionedRecyclerViewAdapter<LocationAdapter.BaseSectionedViewHolder>() {

    fun submitList(countries: List<Section<Server>>) {
        countriesList = countries
        notifyDataSetChanged()
    }

    open class BaseSectionedViewHolder(root: View) : SectionedViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSectionedViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = LayoutInflater.from(parent.context).inflate(R.layout.item_location_header, parent, false)
                LocationListHeaderItemViewHolder(binding, locationClickListener)
            }
            VIEW_TYPE_ITEM -> {
                val binding = LayoutInflater.from(parent.context).inflate(R.layout.item_location_subheader, parent, false)
                LocationListSubHeaderItemViewHolder(binding, locationClickListener)
            }
            else -> throw Exception("No other view holders allowed here")
        }
    }

    inner  class LocationListSubHeaderItemViewHolder(
            private val view: View,
            private val locationClickListener: LocationClickListener
    ) : BaseSectionedViewHolder(view) {

        fun bind(model: Server, isConnected: Boolean) {
            view.childrenRecursiveSequence().forEach {
                it.setOnClickListener {
                    locationClickListener.onLocationActionClick(model)
                }
            }
            if (isConnected) {
                view.ic_tick.visibility = View.VISIBLE
            } else {
                view.ic_tick.visibility = View.GONE
            }
            view.country_city_name.text = Html.fromHtml("${model.location_.city}-${model.location_.country}")
        }

    }

    inner class LocationListHeaderItemViewHolder(
            private val view: View,
            private val locationClickListener: LocationClickListener
    ) : BaseSectionedViewHolder(view) {

        fun bind(model: Location, isExpanded:Boolean) {
            view.country_flag.load("https://www.countryflags.io/${model.country_code}/flat/64.png")
            view.country_name.text = Html.fromHtml(model.country)
            view.country_name.setRightDrawable(if (isExpanded) R.drawable.ic_caret_collapase else R.drawable.ic_caret_expand)
            itemView.setOnClickListener {
                toggleSectionExpanded(relativePosition.section())
            }
            itemView.setOnClickListener {
                toggleSectionExpanded(relativePosition.section())
            }
        }

    }

    override fun getSectionCount(): Int {
        return countriesList.count()
    }

    override fun getItemCount(section: Int): Int {
        return countriesList[section].data.size
    }

    override fun onBindHeaderViewHolder(holder: BaseSectionedViewHolder?, section: Int, expanded: Boolean) {
        (holder as LocationListHeaderItemViewHolder).bind(countriesList[section].location, expanded)
    }

    override fun onBindFooterViewHolder(holder: BaseSectionedViewHolder?, section: Int) {

    }

    override fun onBindViewHolder(holder: BaseSectionedViewHolder?, section: Int, relativePosition: Int, absolutePosition: Int) {
        val serverToBind = countriesList[section].data[relativePosition]
        val isConnected = serverToBind == currentServer
        (holder as LocationListSubHeaderItemViewHolder).bind(serverToBind, isConnected)
    }

}