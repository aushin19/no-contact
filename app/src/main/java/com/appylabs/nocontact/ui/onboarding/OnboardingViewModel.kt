package com.appylabs.nocontact.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appylabs.nocontact.data.NoContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: NoContactRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Next -> goNext()
            OnboardingAction.Back -> goBack()
            is OnboardingAction.SetBreakupDate -> updateDate(action.millis, isBreakupDate = true)
            is OnboardingAction.SetNcStartDate -> updateDate(action.millis, isBreakupDate = false)
            is OnboardingAction.SelectBreakupType -> update { copy(breakupType = action.type) }
            is OnboardingAction.ToggleTrigger -> toggleTrigger(action.trigger)
            is OnboardingAction.SetReasonAnchor -> update { copy(reasonAnchor = action.reason.take(180)) }
            is OnboardingAction.SelectPledge -> update { copy(pledgeText = action.pledge) }
            is OnboardingAction.SelectStreakGoal -> update { copy(streakGoal = action.goal) }
            is OnboardingAction.SelectDangerTime -> update { copy(dangerTimeWindow = action.window) }
            is OnboardingAction.SetCheckinTime -> update { copy(checkinTime = action.time) }
            is OnboardingAction.SelectMood -> update { copy(baselineMood = action.mood) }
            is OnboardingAction.SelectAffirmationStyle -> update { copy(affirmationStyle = action.style) }
            is OnboardingAction.SetAffirmationTime -> update { copy(affirmationTime = action.time) }
            is OnboardingAction.SelectRelapseHistory -> update { copy(relapseHistory = action.history) }
            is OnboardingAction.SelectContactRisk -> update { copy(contactRisk = action.risk) }
            is OnboardingAction.SelectWidgetPreference -> update { copy(widgetPreference = action.preference) }
            is OnboardingAction.SelectPracticeTrigger -> update {
                copy(practiceTriggerTag = action.trigger, sosPracticeCompleted = false)
            }
            OnboardingAction.CompletePractice -> update { copy(sosPracticeCompleted = true) }
            OnboardingAction.Submit -> submit()
            OnboardingAction.ClearError -> update { copy(errorMessage = null) }
        }
    }

    private fun goNext() {
        _uiState.update { state ->
            if (!state.canContinue || state.isLastPage) {
                state
            } else {
                state.copy(currentPageIndex = state.currentPageIndex + 1, errorMessage = null)
            }
        }
    }

    private fun goBack() {
        _uiState.update { state ->
            if (!state.canGoBack) {
                state
            } else {
                state.copy(currentPageIndex = state.currentPageIndex - 1, errorMessage = null)
            }
        }
    }

    private fun updateDate(millis: Long, isBreakupDate: Boolean) {
        if (!isNotFuture(millis)) {
            update { copy(errorMessage = "Date must be today or earlier.") }
            return
        }
        update {
            if (isBreakupDate) {
                copy(breakupDateMillis = millis, errorMessage = null)
            } else {
                copy(ncStartDateMillis = millis, errorMessage = null)
            }
        }
    }

    private fun toggleTrigger(trigger: TriggerTag) {
        update {
            val next = if (trigger in triggerTags) {
                triggerTags - trigger
            } else {
                triggerTags + trigger
            }
            copy(triggerTags = next)
        }
    }

    private fun submit() {
        val snapshot = _uiState.value
        if (!snapshot.allInputsValid || snapshot.isSaving) {
            _uiState.update { it.copy(errorMessage = "Complete every onboarding step before continuing.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                repository.saveProfile(snapshot.toBreakupProfileEntity(System.currentTimeMillis()))
            }.onSuccess {
                _events.emit(OnboardingEvent.ProfileSaved)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        errorMessage = "Could not save your onboarding. Try again."
                    )
                }
            }
        }
    }

    private fun update(reducer: OnboardingUiState.() -> OnboardingUiState) {
        _uiState.update { it.reducer() }
    }

    class Factory(
        private val repository: NoContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                return OnboardingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface OnboardingEvent {
    data object ProfileSaved : OnboardingEvent
}
