package com.appylabs.nocontact.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mood_checkins",
    indices = [Index(value = ["date"], unique = true)]
)
data class MoodCheckinEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "mood_tag") val moodTag: String,     // DEVASTATED | SAD | NUMB | OKAY | STRONG
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "date") val date: String,            // "YYYY-MM-DD"
    @ColumnInfo(name = "created_at") val createdAt: Long
)
