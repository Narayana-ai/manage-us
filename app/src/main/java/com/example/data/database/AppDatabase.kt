package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DailyTemplateDao
import com.example.data.dao.PhaseDao
import com.example.data.model.DailyTemplate
import com.example.data.model.Phase

@Database(entities = [Phase::class, DailyTemplate::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phaseDao(): PhaseDao
    abstract fun dailyTemplateDao(): DailyTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "phase24_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
