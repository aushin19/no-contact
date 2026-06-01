package com.appylabs.nocontact.data

import kotlinx.coroutines.flow.Flow

class NoContactRepository(
    private val breakupProfileDao: BreakupProfileDao,
    private val journalEntryDao: JournalEntryDao,
    private val streakLogDao: StreakLogDao,
    private val sosSessionDao: SosSessionDao,
    private val moodCheckinDao: MoodCheckinDao
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

    suspend fun updateBreakupType(type: String) {
        breakupProfileDao.updateBreakupType(type)
    }

    suspend fun updateDarkMode(override: Int?) {
        breakupProfileDao.updateDarkMode(override)
    }

    suspend fun clearProfile() {
        breakupProfileDao.clearProfile()
        journalEntryDao.clearAll()
        streakLogDao.clearAll()
        sosSessionDao.clearAll()
        moodCheckinDao.clearAll()
    }

    // Streak log methods
    suspend fun saveStreakLog(log: StreakLogEntity) = streakLogDao.insert(log)
    val streakLogs: Flow<List<StreakLogEntity>> = streakLogDao.getAll()
    suspend fun getTotalRelapseCount(): Int = streakLogDao.getTotalRelapseCount()

    // SOS session methods
    suspend fun saveSosSession(session: SosSessionEntity) = sosSessionDao.insert(session)
    val sosSessions: Flow<List<SosSessionEntity>> = sosSessionDao.getAll()
    suspend fun getResistCountSince(since: Long): Int = sosSessionDao.getResistCountSince(since)

    // Mood check-in methods
    suspend fun saveMoodCheckin(checkin: MoodCheckinEntity) = moodCheckinDao.upsert(checkin)
    suspend fun getMoodCheckinForDate(date: String): MoodCheckinEntity? = moodCheckinDao.getCheckinForDate(date)
    val moodCheckins: Flow<List<MoodCheckinEntity>> = moodCheckinDao.getAll()

    // Journal methods
    val journalEntries: Flow<List<JournalEntryEntity>> = journalEntryDao.getAll()

    fun recentJournalEntries(limit: Int): Flow<List<JournalEntryEntity>> = journalEntryDao.getRecent(limit)

    suspend fun saveJournalEntry(entry: JournalEntryEntity) = journalEntryDao.upsert(entry)

    suspend fun deleteJournalEntry(entry: JournalEntryEntity) = journalEntryDao.delete(entry)

    suspend fun getJournalEntryById(id: Int): JournalEntryEntity? = journalEntryDao.getById(id)

    suspend fun clearJournalEntries() = journalEntryDao.clearAll()
}
