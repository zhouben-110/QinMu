package com.qinmu.eyecare.data.model

import android.media.RingtoneManager

/**
 * 提醒提示音效枚举
 */
enum class RestSoundEffect(
    val displayName: String,
    val ringtoneType: Int?
) {
    SYSTEM_NOTIFICATION("系统默认通知音", RingtoneManager.TYPE_NOTIFICATION),
    SYSTEM_ALARM("系统闹钟提示音", RingtoneManager.TYPE_ALARM),
    SYSTEM_RINGTONE("系统电话铃声", RingtoneManager.TYPE_RINGTONE),
    CUSTOM_AUDIO("自定义 1-3秒 MP3 音频", null),
    MUTE("静音 (无声音提醒)", null)
}
