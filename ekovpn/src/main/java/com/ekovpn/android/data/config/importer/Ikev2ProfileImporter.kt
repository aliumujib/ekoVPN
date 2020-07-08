/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.importer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ekovpn.android.data.config.IkeV2
import com.ekovpn.android.data.config.ServerLocation
import com.ekovpn.android.data.config.VPNServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnType
import org.strongswan.android.logic.TrustedCertificateManager
import org.strongswan.android.security.TrustedCertificateEntry
import java.io.FileNotFoundException
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.inject.Inject

class Ikev2ProfileImporter @Inject constructor(val context: Context) {

    fun importServerConfig(fileUri: Uri, location: ServerLocation, ikeV2: IkeV2): Flow<Result<VPNServer>> {
        return flow {
            try {
                val result = parseCertificate(fileUri)
                val alias = "${location.city}-${location.country}"
                if (result != null) {
                    if (storeCertificate(alias, result)) {
                        emit(Result.success(VPNServer.IkeV2Server(makeProfile(result, location, ikeV2), location)))
                    } else {
                        emit(Result.failure<VPNServer>(Exception("An error occurred, error code $CONFIG_PARSING_ERROR")))
                    }
                } else {
                    emit(Result.failure<VPNServer>(Exception("An error occurred, error code $CONFIG_PARSING_ERROR")))
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                emit(Result.failure<VPNServer>(e))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure<VPNServer>(e))
            }
        }
    }


    private fun makeProfile(certificate: X509Certificate, serverLocation: ServerLocation, ikeV2: IkeV2): VpnProfile {
        val vpnProfile = VpnProfile()
        vpnProfile.name = "${serverLocation.city}-${serverLocation.country}-ikeV2"
        vpnProfile.gateway = ikeV2.ip
        vpnProfile.vpnType = VpnType.IKEV2_CERT
        vpnProfile.password = ikeV2.password
        vpnProfile.username = ikeV2.username

        val store = KeyStore.getInstance("LocalCertificateStore")
        store.load(null, null)
        val alias: String = store.getCertificateAlias(certificate)
        vpnProfile.certificateAlias = alias
        Log.d(Ikev2ProfileImporter::class.java.simpleName, "$vpnProfile , $alias")

//        val certman = TrustedCertificateManager.getInstance().load()
//        val certificates = certman.getCACertificates(TrustedCertificateManager.TrustedCertificateSource.LOCAL)
//        val selected: MutableList<TrustedCertificateEntry>
//
//        selected = ArrayList()
//        for ((key, value) in certificates) {
//            selected.add(TrustedCertificateEntry(key, value))
//            Log.d("WG", "$key")
//        }
//
//        selected.sort()
//        Log.d(Ikev2ProfileImporter::class.java.simpleName, "$selected")

        return vpnProfile
    }


    private fun parseCertificate(uri: Uri): X509Certificate? {
        var certificate: X509Certificate? = null
        try {
            val factory = CertificateFactory.getInstance("X.509")
            val `in`: InputStream = context.contentResolver.openInputStream(uri)!!
            certificate = factory.generateCertificate(`in`) as X509Certificate
            /* we don't check whether it's actually a CA certificate or not */
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return certificate
    }


    /**
     * Try to store the given certificate in the KeyStore.
     *
     * @param certificate
     * @return whether it was successfully stored
     */
    private fun storeCertificate(alias: String, certificate: X509Certificate): Boolean {
        return try {
            val store = KeyStore.getInstance("LocalCertificateStore")
            store.load(null, null)
            store.setCertificateEntry(alias, certificate)
            TrustedCertificateManager.getInstance().reset()
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val FILE_IMPORT_ERROR = -2
        const val GENERIC_ERROR = -1
        const val CONFIG_PARSING_ERROR = -3
    }

}