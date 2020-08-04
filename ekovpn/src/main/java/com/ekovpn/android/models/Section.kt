package com.ekovpn.android.models

data class Section<T>(var location:Location, val data: MutableList<T>)