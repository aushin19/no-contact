package com.appylabs.nocontact.ui.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingModelsTest {
    @Test
    fun emptyStateRequiresPersonalizationBeforeSubmit() {
        val state = OnboardingUiState()

        assertTrue(state.isPageValid(0))
        assertFalse(state.allInputsValid)
        assertFalse(state.isPageValid(14))
    }

    @Test
    fun futureDateIsRejected() {
        val tomorrow = System.currentTimeMillis() + 48 * 60 * 60 * 1000

        assertFalse(isNotFuture(tomorrow))
    }

    @Test
    fun incompleteProfileCannotMapToEntity() {
        assertThrows(IllegalArgumentException::class.java) {
            OnboardingUiState().toBreakupProfileEntity(nowMillis = 1000L)
        }
    }

    @Test
    fun completeProfileMapsToRoomEntity() {
        val state = completeState()
        val entity = state.toBreakupProfileEntity(nowMillis = 12345L)

        assertEquals("TOXIC", entity.breakupType)
        assertEquals("LATE_NIGHT,PROFILE", entity.triggerTagsCsv)
        assertEquals("Protect my peace.", entity.reasonAnchor)
        assertEquals(30, entity.streakGoalDays)
        assertEquals("20:30", entity.notifCheckinTime)
        assertEquals("08:15", entity.notifAffirmationTime)
        assertEquals("CHECK_PROFILE", entity.contactRisk)
        assertEquals(12345L, entity.practiceCompletedAtMillis)
        assertEquals(12345L, entity.createdAt)
    }

    private fun completeState(): OnboardingUiState {
        return OnboardingUiState(
            breakupType = BreakupType.TOXIC,
            triggerTags = linkedSetOf(TriggerTag.LATE_NIGHT, TriggerTag.PROFILE),
            reasonAnchor = "  Protect my peace.  ",
            pledgeText = "Today, I protect my peace.",
            streakGoal = StreakGoalDays.THIRTY,
            dangerTimeWindow = DangerTimeWindow.LATE_NIGHT,
            checkinTime = "20:30",
            baselineMood = MoodTag.SAD,
            affirmationStyle = AffirmationStyle.DIRECT,
            affirmationTime = "08:15",
            relapseHistory = RelapseHistory.MANY_TIMES,
            contactRisk = ContactRisk.CHECK_PROFILE,
            widgetPreference = WidgetPreference.MEDIUM,
            practiceTriggerTag = TriggerTag.LATE_NIGHT,
            sosPracticeCompleted = true
        )
    }
}
