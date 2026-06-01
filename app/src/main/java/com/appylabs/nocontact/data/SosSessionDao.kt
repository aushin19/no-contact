package com.appylabs.nocontact.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SosSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SosSessionEntity)

    @Query("SELECT * FROM sos_sessions ORDER BY created_at DESC")
    fun getAll(): Flow<List<SosSessionEntity>>

    @Query("SELECT COUNT(*) FROM sos_sessions WHERE outcome = 'RESISTED' AND created_at > :since")
    suspend fun getResistCountSince(since: Long): Int

    @Query("DELETE FROM sos_sessions")
    suspend fun clearAll()
}
