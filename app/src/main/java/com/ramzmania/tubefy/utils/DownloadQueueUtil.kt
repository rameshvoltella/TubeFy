package com.ramzmania.tubefy.utils

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.ramzmania.tubefy.data.dto.DownloadTask
import com.ramzmania.tubefy.notification.DownloadNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.getValue

object DownloadQueueUtil {

    private val client = OkHttpClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val semaphore = Semaphore(2)

    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks

    private lateinit var appContext: Context
    private val notificationManager by lazy {
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        DownloadNotificationHelper.createChannel(appContext)
    }

    fun enqueue(url: String, filePath: String) {
        val id = UUID.randomUUID().toString()
        val notifyId = id.hashCode()

        val task = DownloadTask(id, url, filePath)
        _tasks.update { it + task }

        scope.launch {
            semaphore.acquire()
            try {
                download(task, notifyId)
                Log.d("TAKKIO","<<<<error0000000")

            } finally {
                Log.d("TAKKIO","<<<<error")
                semaphore.release()
            }
        }
    }

    private suspend fun download(task: DownloadTask, notifyId: Int) {
        try {
            val request = Request.Builder().url(task.url).build()
            val response = client.newCall(request).execute()

            val body = response.body ?: throw IOException()

            val total = body.contentLength()
            var downloaded = 0L

            val file = File(task.filePath)
            file.parentFile?.mkdirs()
            Log.d("TAKKIO","<<<<errormkdir")

            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read

                        val progress = (downloaded * 100 / total).toInt()
                        Log.d("TAKKIO","<<<<errormkdi progress"+progress)

                        updateProgress(task.id, progress)

                        notificationManager.notify(
                            notifyId,
                            DownloadNotificationHelper.build(
                                appContext,
                                notifyId,
                                progress,
                                file.name
                            )
                        )
                    }
                }
            }

            notificationManager.notify(
                notifyId,
                DownloadNotificationHelper.completed(appContext, file.name)
            )

            markCompleted(task.id)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TAKKIO","<<<<error0000000"+e.printStackTrace())

            markFailed(task.id)
        }
    }

    private fun updateProgress(id: String, progress: Int) {
        _tasks.update {
            it.map { if (it.id == id) it.copy(progress = progress) else it }
        }
    }

    private fun markCompleted(id: String) {
        _tasks.update {
            it.map { if (it.id == id) it.copy(progress = 100, isCompleted = true) else it }
        }
    }

    private fun markFailed(id: String) {
        _tasks.update {
            it.map { if (it.id == id) it.copy(isFailed = true) else it }
        }
    }
}
fun String.toSafeFileName(): String {
    return this
        .trim()
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")   // forbidden chars
        .replace(Regex("\\s+"), " ")              // extra spaces
        .replace(Regex("[^a-zA-Z0-9._ -]"), "")   // remove weird symbols
        .take(150)                                // avoid too long names
}