/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.config.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.ekovpn.wireguard.WireGuardInitializer
import com.ekovpn.wireguard.repo.EkoTunnel
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

class WireGuardConfigImporter @Inject constructor(private val context: Context) {

    fun importConfigProfile(uri: Uri): EkoTunnel {
        val contentResolver = context.contentResolver
        val columns = arrayOf(OpenableColumns.DISPLAY_NAME)
        var name = ""
        var tunnel: EkoTunnel? = null
        contentResolver.query(uri, columns, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                name = cursor.getString(0)
            }
        }
        if (name.isEmpty()) {
            name = Uri.decode(uri.lastPathSegment)
        }
        var idx = name.lastIndexOf('/')
        if (idx >= 0) {
            require(idx < name.length - 1) { "Invalid config file name error" }
            name = name.substring(idx + 1)
        }
        val isZip = name.toLowerCase(Locale.ROOT).endsWith(".zip")
        if (name.toLowerCase(Locale.ROOT).endsWith(".conf")) {
            name = name.substring(0, name.length - ".conf".length)
        } else {
            require(isZip) { "Invalid config file extension error" }
        }

        if (isZip) {
            ZipInputStream(contentResolver.openInputStream(uri)).use { zip ->
                val reader = BufferedReader(InputStreamReader(zip, StandardCharsets.UTF_8))
                var entry: ZipEntry?
                while (true) {
                    entry = zip.nextEntry ?: break
                    name = entry.name
                    idx = name.lastIndexOf('/')
                    if (idx >= 0) {
                        if (idx >= name.length - 1) {
                            continue
                        }
                        name = name.substring(name.lastIndexOf('/') + 1)
                    }
                    if (name.toLowerCase(Locale.ROOT).endsWith(".conf")) {
                        name = name.substring(0, name.length - ".conf".length)
                    } else {
                        continue
                    }
                    val zipConfiguration = Config.parse(reader)
                    tunnel = WireGuardInitializer.getTunnelManager().create(name, zipConfiguration)
                }
            }
        } else {
            try {
                tunnel = WireGuardInitializer.getTunnelManager().create(
                        name,
                        Config.parse(contentResolver.openInputStream(uri)!!))

            } catch (e: Exception) {
                Log.d(WireGuardConfigImporter::class.java.simpleName, "Error making config $name")
                WireGuardInitializer.getTunnelManager().deleteAll()
                throw e
            }
//            tunnel = WireGuardInitializer.getTunnelManager().create(
//                    name,
//                    Config.parse(contentResolver.openInputStream(uri)!!))
        }
        val config = tunnel?.config
        tunnel?.setConfig(buildNewConfig(config!!))
        Log.d(WireGuardConfigImporter::class.java.simpleName, "EXCLUDED = ${tunnel!!.config!!.`interface`.excludedApplications}")
        return tunnel!!
    }

    private fun buildNewConfig(other: Config):Config{
        return Config.Builder()
                .setInterface(buildNewInterface(other.`interface`))
                .addPeers(other.peers)
                .build()
    }

    private fun buildNewPeer(other: Peer): Peer {
        val builder = Peer.Builder()
        builder.apply {
            addAllowedIps(other.allowedIps)
            setEndpoint(other.endpoint.get())
            setPersistentKeepalive(other.persistentKeepalive.get())
            setPreSharedKey(other.preSharedKey.get())
            setPublicKey(other.publicKey)
        }
        return builder.build()
    }

    private fun buildNewInterface(other: Interface): Interface {
        val builder = Interface.Builder()
        builder.apply {
            addAddresses(other.addresses)
            addDnsServers(other.dnsServers)
            excludeApplications(listOf("com.ekovpn.android"))
            includeApplications(other.includedApplications)
            setKeyPair(other.keyPair)
        }
        return builder.build()
    }

}