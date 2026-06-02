package com.appylabs.nocontact.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appylabs.nocontact.data.NoContactRepository
import com.appylabs.nocontact.ui.milestones.BadgeState
import com.appylabs.nocontact.ui.milestones.MilestoneTargets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayActivity(
    val label: String,
    val hasEntry: Boolean,
    val isToday: Boolean
)

/**
 * One data point on the mood trend chart.
 * [dayLabel] = "Today", "Yesterday", "2 days ago" …
 * [moodLevel] = 1 (best) … 5 (worst), or 0 if no entry that day.
 */
data class MoodChartPoint(
    val dayLabel: String,
    val mood: String?,
    val moodLevel: Int
)

data class ProgressUiState(
    val streakDisplay: String = "0d 0h 0m 0s",
    val currentDays: Long = 0L,
    val nextMilestoneDays: Int = 1,
    val progressToNext: Float = 0f,
    val daysToNext: Long = 0L,
    val weekActivity: List<DayActivity> = emptyList(),
    /** Last 5 days newest-first, index 0 = today. */
    val moodChartData: List<MoodChartPoint> = emptyList(),
    val totalJournalEntries: Int = 0,
    val badgesEarned: Int = 0,
    val totalBadges: Int = MilestoneTargets.size,
    val badges: List<BadgeState> = emptyList(),
    val hasProfile: Boolean = false
)

class ProgressViewModel(private val repository: NoContactRepository) : ViewModel() {

    private val nowMillis = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                nowMillis.value = System.currentTimeMillis()
            }
        }
    }

    val uiState: StateFlow<ProgressUiState> = combine(
        repository.profile,
        repository.journalEntries,
        nowMillis
    ) { profile, entries, now ->
        if (profile == null) return@combine ProgressUiState()

        val start = profile.ncStartDateMillis
        val elapsed = (now - start).coerceAtLeast(0L)
        val totalDays = TimeUnit.MILLISECONDS.toDays(elapsed)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60

        // Badges
        val badges = MilestoneTargets.map { target ->
            val achieved = totalDays >= target
            val achievedAtMillis = start + TimeUnit.DAYS.toMillis(target.toLong())
            BadgeState(
                days = target,
                label = "$target ${if (target == 1) "Day" else "Days"}",
                achieved = achieved,
                achievedDateLabel = if (achieved) formatShortDate(achievedAtMillis) else "",
                achievedAtMillis = if (achieved) achievedAtMillis else 0L
            )
        }
        val nextBadge = badges.firstOrNull { !it.achieved }
        val nextMilestoneDays = nextBadge?.days ?: MilestoneTargets.last()
        val progressToNext = if (nextBadge != null) {
            (totalDays.toFloat() / nextMilestoneDays.toFloat()).coerceIn(0f, 1f)
        } else 1f
        val daysToNext = if (nextBadge != null) (nextMilestoneDays - totalDays).coerceAtLeast(0L) else 0L

        // Week activity (Mon–Sun of current calendar week)
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val today = dayOfYear(now)
        val entryDays: Set<Long> = entries.map { dayOfYear(it.createdAtMillis) }.toSet()
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val weekActivity = (0..6).map { i ->
            val dayMillis = weekStart.timeInMillis + i * 86_400_000L
            DayActivity(
                label = dayLabels[i],
                hasEntry = dayOfYear(dayMillis) in entryDays,
                isToday = dayOfYear(dayMillis) == today
            )
        }

        // Mood chart: most-recent entry mood for each of the last 5 days
        val moodChartData = (0..4).map { daysBack ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysBack)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + 86_400_000L
            val mood = entries
                .filter { it.createdAtMillis in dayStart until dayEnd && it.mood.isNotBlank() }
                .maxByOrNull { it.createdAtMillis }
                ?.mood
            MoodChartPoint(
                dayLabel = when (daysBack) { 0 -> "Today"; 1 -> "Yesterday"; else -> "$daysBack days ago" },
                mood = mood,
                moodLevel = moodToLevel(mood ?: "")
            )
        }

        ProgressUiState(
            streakDisplay = "${totalDays}d ${hours}h ${minutes}m ${seconds}s",
            currentDays = totalDays,
            nextMilestoneDays = nextMilestoneDays,
            progressToNext = progressToNext,
            daysToNext = daysToNext,
            weekActivity = weekActivity,
            moodChartData = moodChartData,
            totalJournalEntries = entries.size,
            badgesEarned = badges.count { it.achieved },
            totalBadges = MilestoneTargets.size,
            badges = badges,
            hasProfile = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState()
    )

    class Factory(private val repository: NoContactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProgressViewModel::class.java))
                return ProgressViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}

/** Maps a mood string to a chart level 1–5 (1 = best, 5 = worst). Returns 0 if unrecognised. */
internal fun moodToLevel(mood: String): Int = when (mood) {
    "Very Good", "Happy", "Hopeful", "Grateful", "Proud" -> 1
    "Good", "Okay"                                        -> 2
    "Neutral", "Calm"                                     -> 3
    "Bad", "Sad", "Numb"                                  -> 4
    "Very Bad", "Anxious", "Angry"                        -> 5
    else                                                   -> 0
}

private fun dayOfYear(millis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR)
}

private fun formatShortDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
