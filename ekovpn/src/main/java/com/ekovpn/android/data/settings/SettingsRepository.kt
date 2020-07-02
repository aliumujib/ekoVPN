/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.settings

import com.ekovpn.android.models.Protocol

interface SettingsRepository {

    fun setSelectedProtocol(protocol: Protocol)

    fun saveLastServerId(id: Int)

    fun getLastServerId(): Int

    fun getSelectedProtocol(): Protocol

}