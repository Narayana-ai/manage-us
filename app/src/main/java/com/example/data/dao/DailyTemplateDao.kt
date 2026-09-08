package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTemplateDao {
    @Query("SELECT * FROM daily_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<DailyTemplate>>

    @Query("SELECT * FROM daily_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): DailyTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: DailyTemplate): Long

    @Update
    suspend fun updateTemplate(template: DailyTemplate)

    @Delete
    suspend fun deleteTemplate(template: DailyTemplate)
}
