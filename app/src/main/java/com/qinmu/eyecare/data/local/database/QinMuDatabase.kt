package com.qinmu.eyecare.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.qinmu.eyecare.data.local.dao.UsageLogDao
import com.qinmu.eyecare.data.model.UsageLogEntity

@Database(entities = [UsageLogEntity::class], version = 2, exportSchema = false)
abstract class QinMuDatabase : RoomDatabase() {

    abstract fun usageLogDao(): UsageLogDao

    companion object {
        @Volatile
        private var INSTANCE: QinMuDatabase? = null

        fun getDatabase(context: Context): QinMuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QinMuDatabase::class.java,
                    "qinmu_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
