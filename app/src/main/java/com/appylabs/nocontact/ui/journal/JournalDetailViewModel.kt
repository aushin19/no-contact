package com.appylabs.nocontact.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appylabs.nocontact.data.JournalEntryEntity
import com.appylabs.nocontact.data.NoContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val entry: JournalEntryEntity? = null,
    val isLoading: Boolean = true
)

sealed interface DetailEvent {
    data object Deleted : DetailEvent
}

class JournalDetailViewModel(
    private val repository: NoContactRepository,
    private val entryId: Int
) : ViewModel() {

    val state: StateFlow<DetailUiState> = repository.journalEntries
        .map { entries ->
            DetailUiState(
                entry = entries.find { it.id == entryId },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState()
        )

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    fun delete() {
        val entry = state.value.entry ?: return
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
            _events.emit(DetailEvent.Deleted)
        }
    }

    class Factory(
        private val repository: NoContactRepository,
        private val entryId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JournalDetailViewModel::class.java))
                return JournalDetailViewModel(repository, entryId) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
