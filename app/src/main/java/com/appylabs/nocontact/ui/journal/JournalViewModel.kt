package com.appylabs.nocontact.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appylabs.nocontact.data.JournalEntryEntity
import com.appylabs.nocontact.data.NoContactRepository
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class JournalUiState(
    val entries: List<JournalEntryEntity> = emptyList(),
    val filteredEntries: List<JournalEntryEntity> = emptyList(),
    val searchQuery: String = "",
    val totalEntries: Int = 0,
    val positiveDaysThisMonth: Int = 0,
    val writtenDaysThisMonth: Int = 0,
    val currentStreak: Int = 0
)

// Moods considered positive for stats
private val PositiveMoods = setOf("Hopeful", "Grateful", "Calm", "Proud", "Okay", "Happy", "Good", "Very Good")

class JournalViewModel(private val repository: NoContactRepository) : ViewModel() {

    val uiState: StateFlow<JournalUiState> = repository.journalEntries
        .map { entries -> buildUiState(entries, "") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = JournalUiState()
        )

    // In-memory search filter — applied on the StateFlow's value in the composable
    fun filteredEntries(entries: List<JournalEntryEntity>, query: String): List<JournalEntryEntity> {
        if (query.isBlank()) return entries
        val q = query.trim().lowercase()
        return entries.filter {
            it.title.lowercase().contains(q) || it.body.lowercase().contains(q) || it.mood.lowercase().contains(q)
        }
    }

    class Factory(private val repository: NoContactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JournalViewModel::class.java)) return JournalViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}

private fun buildUiState(entries: List<JournalEntryEntity>, query: String): JournalUiState {
    val now = Calendar.getInstance()
    val thisMonth = now.get(Calendar.MONTH)
    val thisYear = now.get(Calendar.YEAR)

    val monthEntries = entries.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.createdAtMillis }
        cal.get(Calendar.MONTH) == thisMonth && cal.get(Calendar.YEAR) == thisYear
    }

    val writtenDays = monthEntries.map {
        val cal = Calendar.getInstance().apply { timeInMillis = it.createdAtMillis }
        cal.get(Calendar.DAY_OF_MONTH)
    }.toSet().size

    val positiveDays = monthEntries.filter { it.mood in PositiveMoods }.map {
        val cal = Calendar.getInstance().apply { timeInMillis = it.createdAtMillis }
        cal.get(Calendar.DAY_OF_MONTH)
    }.toSet().size

    val streak = computeStreak(entries)

    return JournalUiState(
        entries = entries,
        totalEntries = entries.size,
        positiveDaysThisMonth = positiveDays,
        writtenDaysThisMonth = writtenDays,
        currentStreak = streak
    )
}

private fun computeStreak(entries: List<JournalEntryEntity>): Int {
    if (entries.isEmpty()) return 0
    val days = entries.map {
        val cal = Calendar.getInstance().apply { timeInMillis = it.createdAtMillis }
        cal.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }.toSortedSet(reverseOrder())

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val dayMs = 86_400_000L

    var streak = 0
    var expected = today
    for (day in days) {
        if (day == expected) { streak++; expected -= dayMs }
        else if (day < expected) break
    }
    return streak
}
