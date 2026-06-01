package com.appylabs.nocontact.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_sessions")
data class SosSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "trigger_tags") val triggerTags: String,
    @ColumnInfo(name = "outcome") val outcome: String,       // RESISTED | RELAPSED
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
