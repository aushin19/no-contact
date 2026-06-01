package com.appylabs.nocontact

import android.app.Application
import com.appylabs.nocontact.data.NoContactDatabase
import com.appylabs.nocontact.data.NoContactRepository
import com.appylabs.nocontact.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NoContactApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: NoContactDatabase by lazy {
        NoContactDatabase.create(this)
    }

    val repository: NoContactRepository by lazy {
        NoContactRepository(
            breakupProfileDao = database.breakupProfileDao(),
            journalEntryDao = database.journalEntryDao(),
            streakLogDao = database.streakLogDao(),
            sosSessionDao = database.sosSessionDao(),
            moodCheckinDao = database.moodCheckinDao()
        )
    }

    val notificationScheduler: NotificationScheduler by lazy {
        NotificationScheduler(this)
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            val profile = repository.getProfileOnce() ?: return@launch
            if (profile.notifAffirmationOn) notificationScheduler.scheduleAffirmation(profile.notifAffirmationTime)
            if (profile.notifCheckinOn) notificationScheduler.scheduleCheckIn(profile.notifCheckinTime)
        }
    }
}
