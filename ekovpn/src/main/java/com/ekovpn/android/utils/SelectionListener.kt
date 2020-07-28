package com.ekovpn.android.utils

interface SelectionListener<T> {

    fun select(item: T)

    fun deselect(item: T)

}