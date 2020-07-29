/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.config.importer

import android.net.Uri
import androidx.core.net.toFile
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ConfigParser
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

class OVPNProfileImporter @Inject constructor() {

    fun importServerConfig(fileUri: Uri): VpnProfile {
        val inputStream: InputStream = fileUri.toFile().inputStream()
        val result = doImport(inputStream)
        return result!!
    }

    private fun doImport(inputStream: InputStream): VpnProfile? {
        val configParser = ConfigParser()
        val result: VpnProfile?

        val inputStreamReader = InputStreamReader(inputStream)
        configParser.parseConfig(inputStreamReader)
        result = configParser.convertProfile()
        inputStream.close()
        return result
    }


    companion object {
        const val FILE_IMPORT_ERROR = -2
        const val GENERIC_ERROR = -1
        const val CONFIG_PARSING_ERROR = -3
    }

}