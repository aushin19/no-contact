package com.appylabs.nocontact.ui.milestones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appylabs.nocontact.data.BreakupProfileEntity
import com.appylabs.nocontact.data.NoContactRepository
import java.text.SimpleDateFormat
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

val MilestoneTargets = listOf(1, 3, 7, 14, 30, 60, 90, 180)

data class BadgeState(
    val days: Int,
    val label: String,
    val achieved: Boolean,
    val achievedDateLabel: String,
    val achievedAtMillis: Long = 0L
)

data class MilestoneHistoryState(
    val days: Int,
    val title: String,
    val dateLabel: String,
    val deltaLabel: String
)

data class MilestonesUiState(
    val streakDisplay: String = "0d 0h 0m 0s",
    val currentDays: Long = 0L,
    val badges: List<BadgeState> = emptyList(),
    val history: List<MilestoneHistoryState> = emptyList(),
    val nextMilestoneDays: Int = 1,
    val progressToNext: Float = 0f,
    val daysToNext: Long = 1L,
    val motivationalMessage: String = "Start your no-contact journey!",
    val hasProfile: Boolean = false
)

class MilestonesViewModel(repository: NoContactRepository) : ViewModel() {

    private val nowMillis = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<MilestonesUiState> = combine(
        repository.profile,
        nowMillis
    ) { profile, now ->
        buildUiState(profile, now)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MilestonesUiState()
    )

    init {
        viewModelScope.launch {
            while (true) {
                nowMillis.value = System.currentTimeMillis()
                delay(1_000L)
            }
        }
    }

    class Factory(private val repository: NoContactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MilestonesViewModel::class.java))
                return MilestonesViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}

private fun buildUiState(profile: BreakupProfileEntity?, nowMillis: Long): MilestonesUiState {
    if (profile == null) return MilestonesUiState()

    val start = profile.ncStartDateMillis
    val elapsed = (nowMillis - start).coerceAtLeast(0L)
    val totalDays = TimeUnit.MILLISECONDS.toDays(elapsed)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60

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

    val history = badges
        .filter { it.achieved }
        .reversed()
        .map { badge ->
            val idx = MilestoneTargets.indexOf(badge.days)
            val prev = if (idx > 0) MilestoneTargets[idx - 1] else 0
            val delta = badge.days - prev
            MilestoneHistoryState(
                days = badge.days,
                title = "${badge.label} Milestone",
                dateLabel = formatDate(badge.achievedAtMillis),
                deltaLabel = "+$delta ${if (delta == 1) "day" else "days"}"
            )
        }

    val nextBadge = badges.firstOrNull { !it.achieved }
    val nextMilestoneDays = nextBadge?.days ?: MilestoneTargets.last()
    val progressToNext = if (nextBadge != null) {
        (totalDays.toFloat() / nextMilestoneDays.toFloat()).coerceIn(0f, 1f)
    } else 1f
    val daysToNext = if (nextBadge != null) (nextMilestoneDays - totalDays).coerceAtLeast(0L) else 0L

    val motivationalMessage = when {
        nextBadge == null -> "You've achieved all milestones. Incredible!"
        daysToNext == 0L -> "Milestone unlocked today!"
        daysToNext == 1L -> "Just 1 more day to your next milestone!"
        else -> "You're $daysToNext days away from your next milestone!"
    }

    return MilestonesUiState(
        streakDisplay = "${totalDays}d ${hours}h ${minutes}m ${seconds}s",
        currentDays = totalDays,
        badges = badges,
        history = history,
        nextMilestoneDays = nextMilestoneDays,
        progressToNext = progressToNext,
        daysToNext = daysToNext,
        motivationalMessage = motivationalMessage,
        hasProfile = true
    )
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(millis))

private fun formatShortDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
