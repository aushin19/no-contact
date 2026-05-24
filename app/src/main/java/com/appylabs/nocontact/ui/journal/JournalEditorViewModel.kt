package com.appylabs.nocontact.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appylabs.nocontact.data.JournalEntryEntity
import com.appylabs.nocontact.data.NoContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val title: String = "",
    val body: String = "",
    val mood: String = "",
    val isSaving: Boolean = false,
    val isExistingEntry: Boolean = false
)

sealed interface EditorEvent {
    data object Saved : EditorEvent
    data object Deleted : EditorEvent
}

val JournalMoods = listOf("Hopeful", "Grateful", "Calm", "Proud", "Okay", "Sad", "Anxious", "Numb")

class JournalEditorViewModel(
    private val repository: NoContactRepository,
    private val entryId: Int?
) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    private var existingEntry: JournalEntryEntity? = null

    init {
        if (entryId != null) {
            viewModelScope.launch {
                val entry = repository.getJournalEntryById(entryId)
                if (entry != null) {
                    existingEntry = entry
                    _state.update { it.copy(title = entry.title, body = entry.body, mood = entry.mood, isExistingEntry = true) }
                }
            }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value) }
    fun onBodyChange(value: String) = _state.update { it.copy(body = value) }
    fun onMoodChange(value: String) = _state.update { it.copy(mood = value) }

    fun save() {
        val s = _state.value
        if (s.title.isBlank() && s.body.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val entry = JournalEntryEntity(
                id = existingEntry?.id ?: 0,
                title = s.title.trim(),
                body = s.body.trim(),
                mood = s.mood,
                createdAtMillis = existingEntry?.createdAtMillis ?: System.currentTimeMillis()
            )
            repository.saveJournalEntry(entry)
            _events.emit(EditorEvent.Saved)
        }
    }

    fun delete() {
        val entry = existingEntry ?: return
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
            _events.emit(EditorEvent.Deleted)
        }
    }

    class Factory(
        private val repository: NoContactRepository,
        private val entryId: Int?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JournalEditorViewModel::class.java))
                return JournalEditorViewModel(repository, entryId) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
