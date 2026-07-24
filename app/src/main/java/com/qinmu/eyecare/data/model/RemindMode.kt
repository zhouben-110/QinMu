package com.qinmu.eyecare.data.model

/**
 * 提醒模式枚举
 */
enum class RemindMode(val displayName: String, val description: String) {
    NOTIFICATION("温馨通知提醒", "通过系统通知提醒，带【跳过本次沁目】与【开始休息】按钮，不阻断操作"),
    OVERLAY_WINDOW("强效全屏遮罩", "全屏悬浮倒计时遮罩，引导深呼吸与远眺，也支持【跳过本次沁目】")
}
