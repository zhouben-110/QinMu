package com.qinmu.eyecare.data.model

/**
 * 用户配置模型
 */
data class UserPreferences(
    val remindIntervalMinutes: Int = 20, // 默认连续用眼20分钟提醒
    val restDurationSeconds: Int = 20,    // 默认休息20秒 (20-20-20法则)
    val remindMode: RemindMode = RemindMode.NOTIFICATION, // 默认温馨通知
    val soundEffect: RestSoundEffect = RestSoundEffect.SYSTEM_NOTIFICATION, // 提醒音效
    val isFilterEnabled: Boolean = false, // 护眼滤镜开关
    val filterColorArgb: Long = 0x33FFB74D, // 默认暖橙滤镜色值
    val filterAlpha: Float = 0.2f // 滤镜不透明度
)
