package com.qinmu.eyecare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qinmu.eyecare.data.model.UsageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {

    @Query("SELECT * FROM usage_logs WHERE date = :date")
    suspend fun getLogByDate(date: String): UsageLogEntity?

    @Query("SELECT * FROM usage_logs ORDER BY date DESC LIMIT 7")
    fun getRecent7DaysLogs(): Flow<List<UsageLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: UsageLogEntity)

    @Query("UPDATE usage_logs SET screenOnTimeSeconds = screenOnTimeSeconds + :addedSeconds WHERE date = :date")
    suspend fun addScreenTime(date: String, addedSeconds: Long)

    @Query("UPDATE usage_logs SET xiaoQinCount = xiaoQinCount + 1 WHERE date = :date")
    suspend fun incrementXiaoQinCount(date: String)

    @Query("UPDATE usage_logs SET daQinCount = daQinCount + 1 WHERE date = :date")
    suspend fun incrementDaQinCount(date: String)

    @Query("UPDATE usage_logs SET skipCount = skipCount + 1 WHERE date = :date")
    suspend fun incrementSkipCount(date: String)
}
