package com.qinmu.eyecare.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    /**
     * 检查是否有新版本
     */
    fun checkUpdate(
        context: Context,
        jsonUrl: String,
        onResult: (UpdateInfo?) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(jsonUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    requestMethod = "GET"
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)

                    val remoteVersionCode = json.getInt("versionCode")
                    val currentVersionCode = getCurrentVersionCode(context)

                    if (remoteVersionCode > currentVersionCode) {
                        val info = UpdateInfo(
                            versionCode = remoteVersionCode,
                            versionName = json.optString("versionName", "2.0.0"),
                            updateTitle = json.optString("updateTitle", "发现新版本"),
                            updateContent = json.optString("updateContent", "优化了应用体验"),
                            downloadUrl = json.getString("downloadUrl"),
                            isForceUpdate = json.optBoolean("isForceUpdate", false)
                        )
                        withContext(Dispatchers.Main) { onResult(info) }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) { onResult(null) }
        }
    }

    /**
     * 下载并自动触发系统安装 APK
     */
    fun downloadAndInstallApk(context: Context, downloadUrl: String) {
        try {
            val fileName = "QinMu_Update.apk"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) {
                file.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("沁目 APK 下载中...")
                setDescription("正在下载新版本安装包")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(file))
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            // 监听下载完成广播并触发安装
            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id == downloadId) {
                        installApk(context, file)
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onCompleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(onCompleteReceiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun installApk(context: Context, file: File) {
        if (!file.exists()) return

        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }
}
