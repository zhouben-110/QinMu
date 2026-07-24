package com.qinmu.eyecare.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.qinmu.eyecare.service.EyeProtectionService

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, EyeProtectionService::class.java)

        when (intent.action) {
            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                serviceIntent.action = EyeProtectionService.ACTION_SCREEN_ON
                startService(context, serviceIntent)
            }
            Intent.ACTION_SCREEN_OFF -> {
                serviceIntent.action = EyeProtectionService.ACTION_SCREEN_OFF
                startService(context, serviceIntent)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                serviceIntent.action = EyeProtectionService.ACTION_SCREEN_ON
                startService(context, serviceIntent)
            }
        }
    }

    private fun startService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
