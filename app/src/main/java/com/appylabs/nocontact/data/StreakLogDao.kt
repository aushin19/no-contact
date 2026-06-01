package com.appylabs.nocontact.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: StreakLogEntity)

    @Query("SELECT * FROM streak_log ORDER BY created_at DESC")
    fun getAll(): Flow<List<StreakLogEntity>>

    @Query("SELECT COUNT(*) FROM streak_log")
    suspend fun getTotalRelapseCount(): Int

    @Query("DELETE FROM streak_log")
    suspend fun clearAll()
}
