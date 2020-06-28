/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.importer

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ConfigParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

class OVPNProfileImporter @Inject constructor() {

    fun importServerConfig(fileUri: Uri):Flow<Result<VpnProfile>> {
        return flow {
            try {
                val inputStream: InputStream = fileUri.toFile().inputStream()
                val result = doImport(inputStream)
                if (result != null) {
                    emit(Result.success(result))
                } else {
                    emit(Result.failure<VpnProfile>(Exception("An error occurred, error code $CONFIG_PARSING_ERROR")))
                }
            } catch (e: FileNotFoundException){
                e.printStackTrace()
                emit(Result.failure<VpnProfile>(e))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure<VpnProfile>(e))
            }
        }
    }


    private fun doImport(inputStream: InputStream): VpnProfile? {
        val cp = ConfigParser()
        val result: VpnProfile?
        try {
            val isr = InputStreamReader(inputStream)
            cp.parseConfig(isr)
            result = cp.convertProfile()
            return result

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
        }
        return null
    }


    companion object {
        const val FILE_IMPORT_ERROR = -2
        const val GENERIC_ERROR = -1
        const val CONFIG_PARSING_ERROR = -3
    }

}