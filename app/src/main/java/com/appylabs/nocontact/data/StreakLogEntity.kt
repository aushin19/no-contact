package com.appylabs.nocontact.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_log")
data class StreakLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "streak_start") val streakStart: Long,
    @ColumnInfo(name = "streak_end") val streakEnd: Long,
    @ColumnInfo(name = "streak_days") val streakDays: Int,
    @ColumnInfo(name = "reason_tag") val reasonTag: String?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
