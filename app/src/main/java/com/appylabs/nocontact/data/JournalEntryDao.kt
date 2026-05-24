package com.appylabs.nocontact.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entry ORDER BY created_at_millis DESC")
    fun getAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entry ORDER BY created_at_millis DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entry WHERE id = :id")
    suspend fun getById(id: Int): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntryEntity)

    @Delete
    suspend fun delete(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entry")
    suspend fun clearAll()
}
