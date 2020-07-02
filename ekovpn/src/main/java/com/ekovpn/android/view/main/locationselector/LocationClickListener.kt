package com.ekovpn.android.view.main.locationselector

import com.ekovpn.android.models.Server

interface LocationClickListener {
    fun onLocationActionClick(model: Server)
}