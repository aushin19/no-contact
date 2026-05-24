package com.appylabs.nocontact.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BreakupProfileEntity::class, JournalEntryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class NoContactDatabase : RoomDatabase() {
    abstract fun breakupProfileDao(): BreakupProfileDao
    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        private const val DatabaseName = "breakfree_db"

        fun create(context: Context): NoContactDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                NoContactDatabase::class.java,
                DatabaseName
            )
                .addMigrations(Migration1To2, Migration2To3)
                .build()
        }

        private val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE breakup_profile_new (
                        id INTEGER NOT NULL,
                        breakup_type TEXT NOT NULL,
                        breakup_date_millis INTEGER NOT NULL,
                        nc_start_date_millis INTEGER NOT NULL,
                        notif_affirmation_time TEXT NOT NULL,
                        notif_checkin_time TEXT NOT NULL,
                        notif_affirmation_on INTEGER NOT NULL,
                        notif_checkin_on INTEGER NOT NULL,
                        trigger_tags_csv TEXT NOT NULL,
                        reason_anchor TEXT NOT NULL,
                        pledge_text TEXT NOT NULL,
                        streak_goal_days INTEGER NOT NULL,
                        danger_time_window TEXT NOT NULL,
                        baseline_mood TEXT NOT NULL,
                        affirmation_style TEXT NOT NULL,
                        relapse_history TEXT NOT NULL,
                        contact_risk TEXT NOT NULL,
                        widget_preference TEXT NOT NULL,
                        practice_trigger_tag TEXT NOT NULL,
                        practice_completed_at_millis INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO breakup_profile_new (
                        id,
                        breakup_type,
                        breakup_date_millis,
                        nc_start_date_millis,
                        notif_affirmation_time,
                        notif_checkin_time,
                        notif_affirmation_on,
                        notif_checkin_on,
                        trigger_tags_csv,
                        reason_anchor,
                        pledge_text,
                        streak_goal_days,
                        danger_time_window,
                        baseline_mood,
                        affirmation_style,
                        relapse_history,
                        contact_risk,
                        widget_preference,
                        practice_trigger_tag,
                        practice_completed_at_millis,
                        created_at
                    )
                    SELECT
                        id,
                        breakup_type,
                        breakup_date_millis,
                        nc_start_date_millis,
                        notif_affirmation_time,
                        notif_checkin_time,
                        notif_affirmation_on,
                        notif_checkin_on,
                        trigger_tags_csv,
                        reason_anchor,
                        pledge_text,
                        streak_goal_days,
                        danger_time_window,
                        baseline_mood,
                        affirmation_style,
                        relapse_history,
                        contact_risk,
                        widget_preference,
                        practice_trigger_tag,
                        practice_completed_at_millis,
                        created_at
                    FROM breakup_profile
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE breakup_profile")
                db.execSQL("ALTER TABLE breakup_profile_new RENAME TO breakup_profile")
            }
        }

        private val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS journal_entry (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, mood TEXT NOT NULL, created_at_millis INTEGER NOT NULL)"
                )
            }
        }
    }
}
