package com.qinmu.eyecare.data.model

enum class RestType(val displayName: String) {
    XIAO_QIN("小沁·微休息"),
    DA_QIN("大沁·深度放松")
}

enum class SpecialMode(val displayName: String, val iconRes: String) {
    NONE("正常护眼守护", "🌿"),
    MEETING("会议免打扰模式", "💼"),
    GAME("游戏免打扰模式", "🎮")
}

/**
 * 用户配置模型
 */
data class UserPreferences(
    val remindIntervalMinutes: Int = 20, // 小沁提醒间隔（分钟）
    val restDurationSeconds: Int = 20,    // 小沁休息时长（秒）
    val daQinCycleCount: Int = 3,         // 多少次小沁后触发一次大沁
    val daQinRestSeconds: Int = 180,      // 大沁休息时长（秒，默认3分钟）
    val isDualCycleEnabled: Boolean = true, // 是否开启小沁与大沁智能交替模式
    val manualSpecialMode: SpecialMode = SpecialMode.NONE, // 手动选择的特例模式
    val isAutoGameModeEnabled: Boolean = true, // 是否开启自动检测游戏进入免打扰
    val isAutoMeetingModeEnabled: Boolean = true, // 是否开启自动检测会议进入免打扰
    val remindMode: RemindMode = RemindMode.NOTIFICATION, // 默认温馨通知
    val soundEffect: RestSoundEffect = RestSoundEffect.SYSTEM_NOTIFICATION, // 提醒音效
    val isKeepAliveEnabled: Boolean = true // 是否开启后台 WorkManager 周期性保活巡检开关
)

