package com.appylabs.nocontact.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BreakupProfileDaoTest {
    private lateinit var database: NoContactDatabase
    private lateinit var dao: BreakupProfileDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NoContactDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.breakupProfileDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertProfileReplacesSingletonRow() = runBlocking {
        dao.upsertProfile(profile("TOXIC", 7))
        dao.upsertProfile(profile("GHOSTED", 30))

        val saved = dao.getProfileOnce()

        assertNotNull(saved)
        assertEquals(1, saved?.id)
        assertEquals("GHOSTED", saved?.breakupType)
        assertEquals(30, saved?.streakGoalDays)
    }

    private fun profile(type: String, goalDays: Int): BreakupProfileEntity {
        return BreakupProfileEntity(
            breakupType = type,
            breakupDateMillis = 1L,
            ncStartDateMillis = 1L,
            notifAffirmationTime = "08:00",
            notifCheckinTime = "20:00",
            triggerTagsCsv = "LATE_NIGHT",
            reasonAnchor = "Protect peace.",
            pledgeText = "Today, I protect my peace.",
            streakGoalDays = goalDays,
            dangerTimeWindow = "LATE_NIGHT",
            baselineMood = "SAD",
            affirmationStyle = "DIRECT",
            relapseHistory = "FIRST_TIME",
            contactRisk = "TEXT",
            widgetPreference = "MEDIUM",
            practiceTriggerTag = "LATE_NIGHT",
            practiceCompletedAtMillis = 2L,
            createdAt = 2L
        )
    }
}
