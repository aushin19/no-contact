package com.appylabs.nocontact.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appylabs.nocontact.data.BreakupProfileEntity
import com.appylabs.nocontact.data.NoContactRepository
import com.appylabs.nocontact.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: NoContactRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    val profile: StateFlow<BreakupProfileEntity?> = repository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun updateNoContactStartDate(millis: Long) {
        launchSave("No contact start date updated.") {
            repository.updateNoContactStartDate(millis)
        }
    }

    fun updateAffirmationTime(time: String) {
        launchSave("Affirmation notification time updated.") {
            repository.updateAffirmationTime(time)
            if (profile.value?.notifAffirmationOn == true) {
                scheduler.scheduleAffirmation(time)
            }
        }
    }

    fun updateCheckInTime(time: String) {
        launchSave("Mood check-in reminder updated.") {
            repository.updateCheckInTime(time)
            if (profile.value?.notifCheckinOn == true) {
                scheduler.scheduleCheckIn(time)
            }
        }
    }

    fun updateAffirmationNotifications(enabled: Boolean) {
        launchSave(if (enabled) "Affirmation notifications enabled." else "Affirmation notifications disabled.") {
            repository.updateAffirmationNotifications(enabled)
            val time = profile.value?.notifAffirmationTime.orEmpty()
            if (enabled) scheduler.scheduleAffirmation(time) else scheduler.cancelAffirmation()
        }
    }

    fun updateCheckInNotifications(enabled: Boolean) {
        launchSave(if (enabled) "Mood check-ins enabled." else "Mood check-ins disabled.") {
            repository.updateCheckInNotifications(enabled)
            val time = profile.value?.notifCheckinTime.orEmpty()
            if (enabled) scheduler.scheduleCheckIn(time) else scheduler.cancelCheckIn()
        }
    }

    fun updateBreakupType(type: String) {
        launchSave("Breakup type updated.") {
            repository.updateBreakupType(type)
        }
    }

    fun updateDarkMode(override: Int?) {
        viewModelScope.launch {
            runCatching { repository.updateDarkMode(override) }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            runCatching {
                repository.clearProfile()
                scheduler.cancelAffirmation()
                scheduler.cancelCheckIn()
            }.onSuccess {
                _events.emit(SettingsEvent.ResetComplete)
            }.onFailure {
                _events.emit(SettingsEvent.Message("Could not reset data. Try again."))
            }
        }
    }

    fun showPlaceholder(message: String) {
        viewModelScope.launch {
            _events.emit(SettingsEvent.Message(message))
        }
    }

    private fun launchSave(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching {
                block()
            }.onSuccess {
                _events.emit(SettingsEvent.Message(successMessage))
            }.onFailure {
                _events.emit(SettingsEvent.Message("Could not save setting. Try again."))
            }
        }
    }

    class Factory(
        private val repository: NoContactRepository,
        private val scheduler: NotificationScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository, scheduler) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface SettingsEvent {
    data class Message(val text: String) : SettingsEvent
    data object ResetComplete : SettingsEvent
}
