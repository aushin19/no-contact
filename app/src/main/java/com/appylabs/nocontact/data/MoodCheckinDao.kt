package com.appylabs.nocontact.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodCheckinDao {
    // REPLACE enforces one row per calendar day (date column is UNIQUE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(checkin: MoodCheckinEntity)

    @Query("SELECT * FROM mood_checkins WHERE date = :date LIMIT 1")
    suspend fun getCheckinForDate(date: String): MoodCheckinEntity?

    @Query("SELECT * FROM mood_checkins ORDER BY date DESC")
    fun getAll(): Flow<List<MoodCheckinEntity>>

    @Query("DELETE FROM mood_checkins")
    suspend fun clearAll()
}
