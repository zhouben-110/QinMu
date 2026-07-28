package com.qinmu.eyecare.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getDateString(timestampMs: Long): String {
        if (timestampMs <= 0) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    fun formatSecondsToHMS(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(Locale.getDefault(), "%02d小时%02d分钟", hrs, mins)
        } else {
            String.format(Locale.getDefault(), "%02d分钟%02d秒", mins, secs)
        }
    }

    fun formatSecondsToMS(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
