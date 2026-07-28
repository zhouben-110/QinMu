package com.qinmu.eyecare.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.qinmu.eyecare.QinMuApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class KeepAliveWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        try {
            val isAutoStart = runBlocking {
                try {
                    QinMuApplication.instance.preferencesRepository.userPreferencesFlow.first().isAutoStartEnabled
                } catch (e: Exception) {
                    true
                }
            }
            if (!isAutoStart) {
                cancelKeepAliveWork(context)
                return Result.success()
            }

            val serviceIntent = Intent(context, EyeProtectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "qinmu_keep_alive_work"

        fun scheduleKeepAliveWork(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<KeepAliveWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelKeepAliveWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
