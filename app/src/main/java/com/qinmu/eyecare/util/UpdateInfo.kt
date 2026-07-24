package com.qinmu.eyecare.util

/**
 * 远程版本更新信息数据模型
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val updateTitle: String,
    val updateContent: String,
    val downloadUrl: String,
    val isForceUpdate: Boolean = false
)
