/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.settings

import com.ekovpn.android.data.cache.settings.SettingsPrefManager
import com.ekovpn.android.models.Protocol
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(private val settingsPrefManager: SettingsPrefManager) : SettingsRepository {

    override fun setSelectedProtocol(protocol: Protocol) {
        settingsPrefManager.setLastSelectedProtocol(protocol)
    }

    override fun saveLastServerId(id: Int) {
        settingsPrefManager.saveLastServerId(id)
    }

    override fun getLastServerId(): Int {
        return settingsPrefManager.getLastServerId()
    }

    override fun getSelectedProtocol(): Protocol {
        return settingsPrefManager.getLastSelectedProtocol()
    }

}
