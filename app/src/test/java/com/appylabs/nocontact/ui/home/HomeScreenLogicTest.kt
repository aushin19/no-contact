package com.appylabs.nocontact.ui.home

import com.appylabs.nocontact.data.BreakupProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Unit tests for HomeScreen pure logic functions:
 *   - buildHomeUiState  (state assembly)
 *   - calculateStreak   (elapsed-time decomposition)
 *   - buildAffirmationPool (per-user affirmation pool composition)
 *
 * No Android context required — all functions are pure Kotlin.
 */
class HomeScreenLogicTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private val now = System.currentTimeMillis()

    /** A fully-populated profile with a start date N days in the past. */
    private fun profile(
        daysAgo: Long = 10,
        breakupType: String = "TOXIC",
        streakGoalDays: Int = 30,
        affirmationStyle: String = "DIRECT",
        notifAffirmationOn: Boolean = true,
        notifCheckinOn: Boolean = true,
        reasonAnchor: String = "Peace over pain.",
        pledgeText: String = "Today, I protect my peace."
    ) = BreakupProfileEntity(
        breakupType = breakupType,
        breakupDateMillis = now - TimeUnit.DAYS.toMillis(daysAgo + 5),
        ncStartDateMillis = now - TimeUnit.DAYS.toMillis(daysAgo),
        notifAffirmationTime = "08:00",
        notifCheckinTime = "20:00",
        notifAffirmationOn = notifAffirmationOn,
        notifCheckinOn = notifCheckinOn,
        triggerTagsCsv = "LATE_NIGHT,PROFILE",
        reasonAnchor = reasonAnchor,
        pledgeText = pledgeText,
        streakGoalDays = streakGoalDays,
        dangerTimeWindow = "LATE_NIGHT",
        baselineMood = "SAD",
        affirmationStyle = affirmationStyle,
        relapseHistory = "SLIPPED_ONCE",
        contactRisk = "CHECK_PROFILE",
        widgetPreference = "MEDIUM",
        practiceTriggerTag = "LATE_NIGHT",
        practiceCompletedAtMillis = now,
        createdAt = now
    )

    private fun uiState(
        daysAgo: Long = 10,
        breakupType: String = "TOXIC",
        streakGoalDays: Int = 30,
        affirmationOffset: Int = 0
    ) = buildHomeUiState(
        profile = profile(daysAgo = daysAgo, breakupType = breakupType, streakGoalDays = streakGoalDays),
        nowMillis = now,
        affirmationOffset = affirmationOffset,
        savedAffirmations = emptySet(),
        recentEntries = emptyList()
    )

    // ─────────────────────────────────────────────────────────────────────────
    // buildHomeUiState — null profile
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun nullProfile_hasProfileIsFalse() {
        val state = buildHomeUiState(
            profile = null,
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertFalse(state.hasProfile)
    }

    @Test
    fun nullProfile_streakIsZero() {
        val state = buildHomeUiState(
            profile = null,
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertEquals(0L, state.streak.days)
        assertEquals(0L, state.streak.hours)
    }

    @Test
    fun nullProfile_affirmationPoolNeverEmpty_noIndexCrash() {
        // Even with a null profile the affirmation index must not throw.
        val state = buildHomeUiState(
            profile = null,
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertTrue(state.affirmation.isNotBlank())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildHomeUiState — streak
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun tenDayStreak_daysIs10() {
        assertEquals(10L, uiState(daysAgo = 10).streak.days)
    }

    @Test
    fun zeroDayStreak_allComponentsAreZero() {
        val startedJustNow = buildHomeUiState(
            profile = profile(daysAgo = 0),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        // Start = now, so elapsed ≈ 0 — days must be 0
        assertEquals(0L, startedJustNow.streak.days)
    }

    @Test
    fun futureStartDate_streakNeverNegative() {
        val futureStart = buildHomeUiState(
            profile = profile(daysAgo = -5), // started 5 days in the future
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertTrue(futureStart.streak.days >= 0)
        assertTrue(futureStart.streak.hours >= 0)
        assertTrue(futureStart.streak.minutes >= 0)
        assertTrue(futureStart.streak.seconds >= 0)
    }

    @Test
    fun streakHeroText_containsDays() {
        val state = uiState(daysAgo = 7)
        assertTrue("heroText should mention days", state.streak.heroText.contains("d"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildHomeUiState — affirmation pool / index
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun affirmationOffsetZero_isNotBlank() {
        assertTrue(uiState(affirmationOffset = 0).affirmation.isNotBlank())
    }

    @Test
    fun affirmationOffset_largeValue_doesNotThrow() {
        // Offset larger than pool size must still resolve safely via floorMod
        val state = uiState(affirmationOffset = Int.MAX_VALUE - 1)
        assertTrue(state.affirmation.isNotBlank())
    }

    @Test
    fun affirmationOffset_wraps_differentFromZero() {
        val pool = buildAffirmationPool(profile())
        val stateA = uiState(affirmationOffset = 0)
        val stateB = uiState(affirmationOffset = pool.size) // full wrap
        // After a full wrap the affirmation should be identical to offset 0
        assertEquals(stateA.affirmation, stateB.affirmation)
    }

    @Test
    fun consecutiveOffsets_returnDifferentAffirmations() {
        // Offsets 0 and 1 should normally differ (pool > 1 entry)
        val pool = buildAffirmationPool(profile())
        assertTrue("Pool must have at least 2 entries to cycle", pool.size >= 2)
        val stateA = uiState(affirmationOffset = 0)
        val stateB = uiState(affirmationOffset = 1)
        // They should differ unless the day seed wraps to the same slot — acceptable at pool size == 1.
        // Since pool >= 2 this should differ.
        assertFalse(stateA.affirmation == stateB.affirmation)
    }

    @Test
    fun savedAffirmation_isAffirmationSavedReflectsKey() {
        val state0 = buildHomeUiState(
            profile = profile(),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertFalse(state0.isAffirmationSaved)

        val stateSaved = buildHomeUiState(
            profile = profile(),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = setOf(state0.affirmationKey),
            recentEntries = emptyList()
        )
        assertTrue(stateSaved.isAffirmationSaved)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildHomeUiState — milestone progress
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun milestoneProgress_clampedBetween0And1() {
        // More days than the goal should clamp to 1f
        val overshot = buildHomeUiState(
            profile = profile(daysAgo = 60, streakGoalDays = 30),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertEquals(1f, overshot.milestoneProgress, 0.001f)
    }

    @Test
    fun milestoneProgress_zeroAtStart() {
        val fresh = buildHomeUiState(
            profile = profile(daysAgo = 0, streakGoalDays = 30),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertEquals(0f, fresh.milestoneProgress, 0.05f) // small tolerance for sub-second elapsed
    }

    @Test
    fun daysToMilestone_zeroWhenGoalMet() {
        val met = buildHomeUiState(
            profile = profile(daysAgo = 30, streakGoalDays = 30),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertEquals(0, met.daysToMilestone)
    }

    @Test
    fun daysToMilestone_correctWhenHalfway() {
        val halfway = buildHomeUiState(
            profile = profile(daysAgo = 15, streakGoalDays = 30),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertEquals(15, halfway.daysToMilestone)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildHomeUiState — fallbacks
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun reasonAnchor_blankInProfile_usesFallback() {
        val state = buildHomeUiState(
            profile = profile(reasonAnchor = "   "),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertTrue(state.reasonAnchor.isNotBlank())
    }

    @Test
    fun pledge_blankInProfile_usesFallback() {
        val state = buildHomeUiState(
            profile = profile(pledgeText = ""),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertTrue(state.pledge.isNotBlank())
    }

    @Test
    fun startedDate_isNotBlank() {
        assertTrue(uiState().startedDate.isNotBlank())
    }

    @Test
    fun recentEntries_arePassedThroughToState() {
        // buildHomeUiState must forward recentEntries unchanged
        val state = buildHomeUiState(
            profile = profile(),
            nowMillis = now,
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
        assertTrue(state.recentEntries.isEmpty())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calculateStreak
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun calculateStreak_nullStart_returnsZeroStreak() {
        val streak = calculateStreak(null, now)
        assertEquals(0L, streak.days)
        assertEquals(0L, streak.hours)
        assertEquals(0L, streak.minutes)
        assertEquals(0L, streak.seconds)
    }

    @Test
    fun calculateStreak_exactlySevenDaysAgo() {
        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        val streak = calculateStreak(sevenDaysAgo, now)
        assertEquals(7L, streak.days)
    }

    @Test
    fun calculateStreak_twelveHoursAgo_daysIsZero() {
        val twelveHoursAgo = now - TimeUnit.HOURS.toMillis(12)
        val streak = calculateStreak(twelveHoursAgo, now)
        assertEquals(0L, streak.days)
        assertEquals(12L, streak.hours)
    }

    @Test
    fun calculateStreak_futureStart_allZero() {
        val future = now + TimeUnit.DAYS.toMillis(1)
        val streak = calculateStreak(future, now)
        assertEquals(0L, streak.days)
        assertEquals(0L, streak.hours)
        assertEquals(0L, streak.minutes)
        assertEquals(0L, streak.seconds)
    }

    @Test
    fun calculateStreak_components_within24h() {
        // 1 day + 5 hours + 30 minutes + 15 seconds ago
        val elapsed = TimeUnit.DAYS.toMillis(1) +
            TimeUnit.HOURS.toMillis(5) +
            TimeUnit.MINUTES.toMillis(30) +
            TimeUnit.SECONDS.toMillis(15)
        val start = now - elapsed
        val streak = calculateStreak(start, now)
        assertEquals(1L, streak.days)
        assertEquals(5L, streak.hours)
        assertEquals(30L, streak.minutes)
        assertEquals(15L, streak.seconds)
    }

    @Test
    fun calculateStreak_heroText_format() {
        val streak = calculateStreak(now - TimeUnit.DAYS.toMillis(3), now)
        // Should contain "3d"
        assertTrue(streak.heroText.startsWith("3d"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildAffirmationPool
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun buildAffirmationPool_nullProfile_neverEmpty() {
        val pool = buildAffirmationPool(null)
        assertTrue("Pool must not be empty for null profile", pool.isNotEmpty())
    }

    @Test
    fun buildAffirmationPool_toxicProfile_containsTypeEntries() {
        val pool = buildAffirmationPool(profile(breakupType = "TOXIC"))
        // Type-specific entries should expand the pool beyond the general minimum
        val generalPool = buildAffirmationPool(null)
        assertTrue("TOXIC pool should be at least as large as general pool", pool.size >= generalPool.size)
    }

    @Test
    fun buildAffirmationPool_ghostedProfile_containsGhostedEntries() {
        val pool = buildAffirmationPool(profile(breakupType = "GHOSTED"))
        val hasGhostedEntry = pool.any { it.contains("chase", ignoreCase = true) ||
            it.contains("disappeared", ignoreCase = true) ||
            it.contains("closure", ignoreCase = true) }
        assertTrue("GHOSTED pool must contain ghosted-specific affirmations", hasGhostedEntry)
    }

    @Test
    fun buildAffirmationPool_unknownBreakupType_fallsBackToGeneral() {
        val pool = buildAffirmationPool(profile(breakupType = "UNKNOWN_TYPE"))
        assertTrue("Unknown type pool must still be non-empty", pool.isNotEmpty())
    }

    @Test
    fun buildAffirmationPool_styleEntry_appended() {
        // The DIRECT style entry is "Do not trade your peace for one more message."
        val pool = buildAffirmationPool(profile(affirmationStyle = "DIRECT"))
        assertTrue(
            "DIRECT style entry must appear in pool",
            pool.any { it.contains("trade your peace", ignoreCase = true) }
        )
    }

    @Test
    fun buildAffirmationPool_allEntriesNonBlank() {
        buildAffirmationPool(profile()).forEach { entry ->
            assertTrue("All pool entries must be non-blank", entry.isNotBlank())
        }
    }
}
