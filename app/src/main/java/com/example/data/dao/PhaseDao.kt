package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Phase
import kotlinx.coroutines.flow.Flow

@Dao
interface PhaseDao {
    @Query("SELECT * FROM phases WHERE dayDate = :date ORDER BY sortOrder ASC, startMinutes ASC")
    fun getPhasesForDay(date: String): Flow<List<Phase>>

    @Query("SELECT * FROM phases WHERE dayDate = :date ORDER BY sortOrder ASC, startMinutes ASC")
    suspend fun getPhasesForDaySync(date: String): List<Phase>

    @Query("SELECT * FROM phases WHERE id = :id")
    suspend fun getPhaseById(id: Long): Phase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase(phase: Phase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phases: List<Phase>)

    @Update
    suspend fun updatePhase(phase: Phase)

    @Delete
    suspend fun deletePhase(phase: Phase)

    @Query("DELETE FROM phases WHERE id = :id")
    suspend fun deletePhaseById(id: Long)

    @Query("DELETE FROM phases WHERE dayDate = :date")
    suspend fun clearDay(date: String)

    @Query("SELECT * FROM phases")
    fun getAllPhases(): Flow<List<Phase>>
}
