package com.appylabs.nocontact.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breakup_profile")
data class BreakupProfileEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "breakup_type") val breakupType: String,
    @ColumnInfo(name = "breakup_date_millis") val breakupDateMillis: Long,
    @ColumnInfo(name = "nc_start_date_millis") val ncStartDateMillis: Long,
    @ColumnInfo(name = "notif_affirmation_time") val notifAffirmationTime: String,
    @ColumnInfo(name = "notif_checkin_time") val notifCheckinTime: String,
    @ColumnInfo(name = "notif_affirmation_on") val notifAffirmationOn: Boolean = true,
    @ColumnInfo(name = "notif_checkin_on") val notifCheckinOn: Boolean = true,
    @ColumnInfo(name = "trigger_tags_csv") val triggerTagsCsv: String,
    @ColumnInfo(name = "reason_anchor") val reasonAnchor: String,
    @ColumnInfo(name = "pledge_text") val pledgeText: String,
    @ColumnInfo(name = "streak_goal_days") val streakGoalDays: Int,
    @ColumnInfo(name = "danger_time_window") val dangerTimeWindow: String,
    @ColumnInfo(name = "baseline_mood") val baselineMood: String,
    @ColumnInfo(name = "affirmation_style") val affirmationStyle: String,
    @ColumnInfo(name = "relapse_history") val relapseHistory: String,
    @ColumnInfo(name = "contact_risk") val contactRisk: String,
    @ColumnInfo(name = "widget_preference") val widgetPreference: String,
    @ColumnInfo(name = "practice_trigger_tag") val practiceTriggerTag: String,
    @ColumnInfo(name = "practice_completed_at_millis") val practiceCompletedAtMillis: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
