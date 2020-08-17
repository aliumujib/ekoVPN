/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.repositories.config.downloader

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.ekovpn.android.data.repositories.config.IKEv2
import com.ekovpn.android.data.repositories.config.ServerLocation
import com.ekovpn.android.data.repositories.config.ServerSetUp
import com.ekovpn.android.models.Protocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@ExperimentalCoroutinesApi
class FileDownloader @Inject constructor(private val context: Context) {

    private fun getRootDirPath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file: File = ContextCompat.getExternalFilesDirs(context.applicationContext, null)[0]
            file.absolutePath
        } else {
            context.applicationContext.filesDir.absolutePath
        }
    }

    fun downloadIKev2Certificate(serverLocation: ServerLocation, ikeV2: IKEv2): Flow<Result<ServerSetUp>> {
        return downloadConfigFile(serverLocation, Protocol.IKEv2, ikeV2.certificate_url, ikeV2)
    }

    fun downloadOVPNConfig(serverLocation: ServerLocation, protocol: Protocol, configFileURL: String): Flow<Result<ServerSetUp>> {
        return downloadConfigFile(serverLocation, protocol, configFileURL)
    }

    fun downloadWireGuardConfig(serverLocation: ServerLocation, configFileURL: String): Flow<Result<ServerSetUp>> {
        return downloadConfigFile(serverLocation, Protocol.WIREGUARD, configFileURL)
    }

    private fun downloadConfigFile(serverLocation: ServerLocation, protocol: Protocol, configFileURL: String, ikeV2: IKEv2? = null): Flow<Result<ServerSetUp>> {
        val fileName = if (protocol == Protocol.TCP || protocol == Protocol.UDP) {
            "${serverLocation.city}_${serverLocation.country}_${protocol.value}.ovpn".replace(" ", "_")
        } else if(protocol == Protocol.IKEv2) {
            "${serverLocation.city}_${serverLocation.country}_${protocol.value}.pem".replace(" ", "_")
        }else{
            "${serverLocation.city}_${serverLocation.country_code}.conf".replace(" ", "_")
        }
        val channel = ConflatedBroadcastChannel<Result<ServerSetUp>>()

        val filePath = "${getRootDirPath(context)}/${fileName}"
        PRDownloader.download(configFileURL, getRootDirPath(context), fileName)
                .build()
                .setOnStartOrResumeListener { }
                .setOnPauseListener { }
                .setOnProgressListener { }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        GlobalScope.launch(Dispatchers.IO) {
                            Log.d(FileDownloader::class.java.simpleName, "Downloaded: $filePath")
                            if (protocol == Protocol.UDP || protocol == Protocol.TCP) {
                                val result = ServerSetUp.OVPNSetup(File(filePath).toURI().toString(), serverLocation = serverLocation, protocol = protocol)
                                channel.send(Result.success(result))
                            } else if(protocol == Protocol.IKEv2) {
                                val result = ServerSetUp.IkeV2Setup(File(filePath).toURI().toString(), serverLocation = serverLocation, protocol = protocol, ikeV2 = ikeV2!!)
                                channel.send(Result.success(result))
                            }else{
                                val result = ServerSetUp.WireGuardSetup(File(filePath).toURI().toString(), serverLocation = serverLocation, protocol = protocol)
                                channel.send(Result.success(result))
                            }
                        }
                    }

                    override fun onError(error: com.downloader.Error?) {
                        GlobalScope.launch(Dispatchers.IO) {
                            Log.d(FileDownloader::class.java.simpleName, "Error downloading ${configFileURL}, protocol: $protocol :" +
                                    " ${error?.isConnectionError} ${error?.isServerError} ${error?.connectionException?.localizedMessage} ${error?.serverErrorMessage}")
                            channel.send(Result.failure(FileDownloaderException(error.toString())))
                        }
                    }
                })

        return channel.asFlow()
    }

}