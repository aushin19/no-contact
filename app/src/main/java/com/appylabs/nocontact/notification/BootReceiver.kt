package com.appylabs.nocontact.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.appylabs.nocontact.NoContactApplication
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as NoContactApplication
        val profile = runBlocking { app.repository.getProfileOnce() } ?: return
        val scheduler = app.notificationScheduler
        if (profile.notifAffirmationOn) scheduler.scheduleAffirmation(profile.notifAffirmationTime)
        if (profile.notifCheckinOn) scheduler.scheduleCheckIn(profile.notifCheckinTime)
    }
}
