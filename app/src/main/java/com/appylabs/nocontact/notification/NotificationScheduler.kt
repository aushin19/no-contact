package com.appylabs.nocontact.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appylabs.nocontact.MainActivity
import com.appylabs.nocontact.MainActivity.Companion.DEST_MILESTONES
import com.appylabs.nocontact.MainActivity.Companion.EXTRA_DESTINATION
import java.util.Calendar

class NotificationScheduler(private val context: Context) {

    companion object {
        const val CHANNEL_AFFIRMATION = "affirmation"
        const val CHANNEL_CHECKIN = "checkin"
        const val CHANNEL_MILESTONE = "milestone"
        const val EXTRA_TYPE = "notification_type"
        const val TYPE_AFFIRMATION = 1
        const val TYPE_CHECKIN = 2
        private const val REQUEST_AFFIRMATION = 1001
        private const val REQUEST_CHECKIN = 1002
        private const val NOTIFY_ID_MILESTONE = 2001
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAffirmation(time: String) = scheduleDaily(time, REQUEST_AFFIRMATION, TYPE_AFFIRMATION)

    fun scheduleCheckIn(time: String) = scheduleDaily(time, REQUEST_CHECKIN, TYPE_CHECKIN)

    fun cancelAffirmation() = cancel(REQUEST_AFFIRMATION, TYPE_AFFIRMATION)

    fun cancelCheckIn() = cancel(REQUEST_CHECKIN, TYPE_CHECKIN)

    fun fireMilestoneNotification(days: Int, badgeName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureMilestoneChannel(manager)
        val tapIntent = PendingIntent.getActivity(
            context, NOTIFY_ID_MILESTONE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_DESTINATION, DEST_MILESTONES)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.notify(
            NOTIFY_ID_MILESTONE,
            NotificationCompat.Builder(context, CHANNEL_MILESTONE)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("You earned a badge! \uD83C\uDFC6")
                .setContentText("$badgeName — $days days no contact")
                .setContentIntent(tapIntent)
                .setAutoCancel(true)
                .build()
        )
    }

    private fun ensureMilestoneChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MILESTONE,
                    "Milestone Unlocked",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Badge unlock notifications" }
            )
        }
    }

    private fun scheduleDaily(time: String, requestCode: Int, type: Int) {
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        val hour = parts.getOrNull(0) ?: return
        val minute = parts.getOrNull(1) ?: 0

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val pi = buildPendingIntent(requestCode, type)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }

    private fun cancel(requestCode: Int, type: Int) {
        alarmManager.cancel(buildPendingIntent(requestCode, type))
    }

    private fun buildPendingIntent(requestCode: Int, type: Int): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
