package com.qinmu.eyecare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 屏幕用眼与休息历史统计实体
 * 记录格式：YYYY-MM-DD
 */
@Entity(tableName = "usage_logs")
data class UsageLogEntity(
    @PrimaryKey
    val date: String, // 格式: 2026-07-24
    val screenOnTimeSeconds: Long = 0L, // 当日连屏累积秒数
    val restCount: Int = 0,             // 完成休息次数
    val skipCount: Int = 0              // 跳过本次沁目次数
)
