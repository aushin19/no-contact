package com.appylabs.nocontact.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BreakupProfileDao {
    @Query("SELECT * FROM breakup_profile WHERE id = 1")
    fun getProfile(): Flow<BreakupProfileEntity?>

    @Query("SELECT * FROM breakup_profile WHERE id = 1")
    suspend fun getProfileOnce(): BreakupProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: BreakupProfileEntity)

    @Query("UPDATE breakup_profile SET nc_start_date_millis = :millis WHERE id = 1")
    suspend fun updateNoContactStartDate(millis: Long)

    @Query("UPDATE breakup_profile SET notif_affirmation_time = :time WHERE id = 1")
    suspend fun updateAffirmationTime(time: String)

    @Query("UPDATE breakup_profile SET notif_checkin_time = :time WHERE id = 1")
    suspend fun updateCheckInTime(time: String)

    @Query("UPDATE breakup_profile SET notif_affirmation_on = :enabled WHERE id = 1")
    suspend fun updateAffirmationNotifications(enabled: Boolean)

    @Query("UPDATE breakup_profile SET notif_checkin_on = :enabled WHERE id = 1")
    suspend fun updateCheckInNotifications(enabled: Boolean)

    @Query("UPDATE breakup_profile SET breakup_type = :type WHERE id = 1")
    suspend fun updateBreakupType(type: String)

    @Query("UPDATE breakup_profile SET dark_mode_override = :override WHERE id = 1")
    suspend fun updateDarkMode(override: Int?)

    @Query("DELETE FROM breakup_profile WHERE id = 1")
    suspend fun clearProfile()
}
