package com.ekovpn.android.view.main.ads.adapter

interface SelectionListener<T> {

    fun select(item: T)

    fun deselect(item: T)

}