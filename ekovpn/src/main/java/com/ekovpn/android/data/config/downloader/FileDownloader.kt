/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.data.config.downloader

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.ekovpn.android.data.config.IkeV2
import com.ekovpn.android.data.config.ServerLocation
import com.ekovpn.android.data.config.ServerSetUp
import com.ekovpn.android.models.Protocol
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.listener.DownloadListener2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception
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

    fun downloadIkev2Certificate(serverLocation: ServerLocation, protocol: Protocol, configFileURL: String) {

    }

    fun downloadOVPNConfig(serverLocation: ServerLocation, protocol: Protocol, configFileURL: String) {

    }

    fun downloadConfigFile(serverLocation: ServerLocation, protocol: Protocol, configFileURL: String, ikeV2: IkeV2? = null): Flow<Result<ServerSetUp>> {
        val fileName = if (protocol == Protocol.TCP || protocol == Protocol.UDP) {
            "${serverLocation.city}_${serverLocation.country}_${protocol.value}.ovpn"
        } else {
            "${serverLocation.city}_${serverLocation.country}_${protocol.value}.pem"
        }
        val channel = ConflatedBroadcastChannel<Result<ServerSetUp>>()
        val task = DownloadTask.Builder(configFileURL, File(getRootDirPath(context)))
                .setFilename(fileName)
                .setMinIntervalMillisCallbackProcess(30)
                .setPassIfAlreadyCompleted(false)
                .build()

        task.enqueue(object : DownloadListener2() {
            override fun taskStart(task: DownloadTask) {

            }

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
                GlobalScope.launch(Dispatchers.IO) {
                    if (cause == EndCause.COMPLETED) {
                        Log.d(FileDownloader::class.java.simpleName, "Downloaded: ${task.file?.absolutePath}")
                        if (protocol == Protocol.UDP || protocol == Protocol.TCP) {
                            val result = ServerSetUp.OVPNSetup(task.file?.toURI().toString(), serverLocation = serverLocation, protocol = protocol)
                            channel.send(Result.success(result))
                        } else {
                            val result = ServerSetUp.IkeV2Setup(task.file?.toURI().toString(), serverLocation = serverLocation, protocol = protocol, ikeV2 = ikeV2!!)
                            channel.send(Result.success(result))
                        }
                    } else {
                        realCause?.printStackTrace()
                        Log.d(FileDownloader::class.java.simpleName, realCause?.localizedMessage, realCause)
                        channel.send(Result.failure(FileDownloaderException(realCause?.localizedMessage
                                ?: "An error occurred")))
                    }
                }
            }
        })

        return channel.asFlow()
    }

}