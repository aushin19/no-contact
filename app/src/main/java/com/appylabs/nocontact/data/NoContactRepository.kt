package com.appylabs.nocontact.data

import kotlinx.coroutines.flow.Flow

class NoContactRepository(
    private val breakupProfileDao: BreakupProfileDao
) {
    val profile: Flow<BreakupProfileEntity?> = breakupProfileDao.getProfile()

    suspend fun getProfileOnce(): BreakupProfileEntity? {
        return breakupProfileDao.getProfileOnce()
    }

    suspend fun saveProfile(profile: BreakupProfileEntity) {
        breakupProfileDao.upsertProfile(profile.copy(id = 1))
    }

    suspend fun updateNoContactStartDate(millis: Long) {
        breakupProfileDao.updateNoContactStartDate(millis)
    }

    suspend fun updateAffirmationTime(time: String) {
        breakupProfileDao.updateAffirmationTime(time)
    }

    suspend fun updateCheckInTime(time: String) {
        breakupProfileDao.updateCheckInTime(time)
    }

    suspend fun updateAffirmationNotifications(enabled: Boolean) {
        breakupProfileDao.updateAffirmationNotifications(enabled)
    }

    suspend fun updateCheckInNotifications(enabled: Boolean) {
        breakupProfileDao.updateCheckInNotifications(enabled)
    }

    suspend fun clearProfile() {
        breakupProfileDao.clearProfile()
    }
}
