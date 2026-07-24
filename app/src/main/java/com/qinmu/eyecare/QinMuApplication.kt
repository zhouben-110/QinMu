package com.qinmu.eyecare

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.qinmu.eyecare.data.local.database.QinMuDatabase
import com.qinmu.eyecare.data.local.datastore.UserPreferencesRepository

class QinMuApplication : Application() {

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var database: QinMuDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesRepository = UserPreferencesRepository(this)
        database = QinMuDatabase.getDatabase(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "护眼服务常驻通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持护眼监控服务后台运行"
            }

            val remindChannel = NotificationChannel(
                CHANNEL_ID_REMIND,
                "沁目休息提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "连屏使用时长达到设定期限时的休息提醒"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(serviceChannel)
            notificationManager?.createNotificationChannel(remindChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_SERVICE = "qinmu_service_channel"
        const val CHANNEL_ID_REMIND = "qinmu_remind_channel"

        lateinit var instance: QinMuApplication
            private set
    }
}
