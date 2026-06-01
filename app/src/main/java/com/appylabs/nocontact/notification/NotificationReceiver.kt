package com.appylabs.nocontact.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appylabs.nocontact.MainActivity
import com.appylabs.nocontact.MainActivity.Companion.DEST_HOME
import com.appylabs.nocontact.MainActivity.Companion.DEST_HOME_MOOD
import com.appylabs.nocontact.MainActivity.Companion.EXTRA_DESTINATION
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.notification.NotificationScheduler.Companion.CHANNEL_AFFIRMATION
import com.appylabs.nocontact.notification.NotificationScheduler.Companion.CHANNEL_CHECKIN
import com.appylabs.nocontact.notification.NotificationScheduler.Companion.CHANNEL_MILESTONE
import com.appylabs.nocontact.notification.NotificationScheduler.Companion.EXTRA_TYPE
import com.appylabs.nocontact.notification.NotificationScheduler.Companion.TYPE_AFFIRMATION
import com.appylabs.nocontact.notification.NotificationScheduler.Companion.TYPE_CHECKIN
import kotlinx.coroutines.runBlocking

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getIntExtra(EXTRA_TYPE, -1)
        val app = context.applicationContext as NoContactApplication
        val profile = runBlocking { app.repository.getProfileOnce() } ?: return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannels(manager)

        when (type) {
            TYPE_AFFIRMATION -> {
                if (!profile.notifAffirmationOn) return
                manager.notify(
                    TYPE_AFFIRMATION,
                    NotificationCompat.Builder(context, CHANNEL_AFFIRMATION)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle("Your daily affirmation")
                        .setContentText("Open the app for today's message and keep your streak alive.")
                        .setContentIntent(buildTapIntent(context, TYPE_AFFIRMATION, DEST_HOME))
                        .setAutoCancel(true)
                        .build()
                )
                app.notificationScheduler.scheduleAffirmation(profile.notifAffirmationTime)
            }
            TYPE_CHECKIN -> {
                if (!profile.notifCheckinOn) return
                manager.notify(
                    TYPE_CHECKIN,
                    NotificationCompat.Builder(context, CHANNEL_CHECKIN)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle("How are you feeling today?")
                        .setContentText("Take a moment to check in with yourself.")
                        .setContentIntent(buildTapIntent(context, TYPE_CHECKIN, DEST_HOME_MOOD))
                        .setAutoCancel(true)
                        .build()
                )
                app.notificationScheduler.scheduleCheckIn(profile.notifCheckinTime)
            }
        }
    }

    private fun buildTapIntent(context: Context, requestCode: Int, destination: String): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_DESTINATION, destination)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun ensureChannels(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_AFFIRMATION,
                    "Daily Affirmations",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily affirmation reminders" }
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_CHECKIN,
                    "Mood Check-ins",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily mood check-in reminders" }
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MILESTONE,
                    "Milestone Unlocked",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Badge unlock notifications" }
            )
        }
    }
}
