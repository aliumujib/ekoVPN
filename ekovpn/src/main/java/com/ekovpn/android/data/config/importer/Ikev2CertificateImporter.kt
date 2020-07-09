/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.importer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ekovpn.android.data.config.IKEv2
import com.ekovpn.android.data.config.ServerLocation
import com.ekovpn.android.data.config.VPNServer
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnType
import org.strongswan.android.logic.TrustedCertificateManager
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import java.io.File

class Ikev2CertificateImporter @Inject constructor(val context: Context) {

    fun importServerConfig(fileUri: Uri, location: ServerLocation, ikeV2: IKEv2): VPNServer {
        val result = parseCertificate(fileUri)
        val alias = "${location.city}-${location.country}"
        storeCertificate(alias, result!!)
        File(fileUri.path).delete()
        return VPNServer.IkeV2Server(makeProfile(result, location, ikeV2), location)
    }


    private fun makeProfile(certificate: X509Certificate, serverLocation: ServerLocation, ikeV2: IKEv2): VpnProfile {
        val vpnProfile = VpnProfile()
        vpnProfile.name = "${serverLocation.city}-${serverLocation.country}-ikeV2"
        vpnProfile.gateway = ikeV2.ip
        vpnProfile.vpnType = VpnType.IKEV2_EAP
        vpnProfile.password = ikeV2.password
        vpnProfile.username = ikeV2.username

        val store = KeyStore.getInstance("LocalCertificateStore")
        store.load(null, null)
        val alias: String = store.getCertificateAlias(certificate)
        vpnProfile.certificateAlias = alias
        Log.d(Ikev2CertificateImporter::class.java.simpleName, "$vpnProfile , $alias")

        return vpnProfile
    }


    private fun parseCertificate(uri: Uri): X509Certificate? {
        var certificate: X509Certificate? = null
        val factory = CertificateFactory.getInstance("X.509")
        val `in`: InputStream = context.contentResolver.openInputStream(uri)!!
        certificate = factory.generateCertificate(`in`) as X509Certificate
        return certificate
    }


    /**
     * Try to store the given certificate in the KeyStore.
     *
     * @param certificate
     * @return whether it was successfully stored
     */
    private fun storeCertificate(alias: String, certificate: X509Certificate) {
        val store = KeyStore.getInstance("LocalCertificateStore")
        store.load(null, null)
        store.setCertificateEntry(alias, certificate)
        TrustedCertificateManager.getInstance().reset()
    }

    companion object {
        const val FILE_IMPORT_ERROR = -2
        const val GENERIC_ERROR = -1
        const val CONFIG_PARSING_ERROR = -3
    }

}