package com.appylabs.nocontact.ui.onboarding

import androidx.compose.runtime.Immutable
import com.appylabs.nocontact.data.BreakupProfileEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val PageCount = 15

enum class BreakupType(val title: String, val description: String) {
    TOXIC("Toxic / Abusive", "Unhealthy patterns, manipulation, or harm."),
    MUTUAL("Mutual / Grew Apart", "Both of you knew the relationship had ended."),
    GHOSTED("Ghosted / One-sided", "No closure, no clear ending, no fair answer."),
    DIVORCE("Divorce / Long-term", "A marriage or deep long-term bond ended.")
}

enum class TriggerTag(val title: String) {
    LATE_NIGHT("Late night and alone"),
    SONG("A song reminded me"),
    PROFILE("Saw their profile or story"),
    ALCOHOL("Drinking or lowered guard"),
    SAD("Really sad"),
    MEMORIES("Replaying memories"),
    CLOSURE("Want closure or answers"),
    OTHER("Something else")
}

enum class MoodTag(val title: String) {
    DEVASTATED("Devastated"),
    SAD("Sad"),
    NUMB("Numb"),
    OKAY("Okay"),
    STRONG("Strong")
}

enum class AffirmationStyle(val title: String, val sample: String) {
    GENTLE("Gentle", "You can move slowly and still move forward."),
    DIRECT("Direct", "Do not trade your peace for one more message."),
    TOUGH_LOVE("Tough-love", "The pattern ends when you stop feeding it."),
    LOGICAL("Logical", "An urge is a signal, not an instruction."),
    SPIRITUAL("Spiritual", "Release what keeps pulling you from yourself.")
}

enum class RelapseHistory(val title: String, val description: String) {
    FIRST_TIME("First time", "I am starting no contact now."),
    SLIPPED_ONCE("Slipped once", "I broke it once and want structure."),
    MANY_TIMES("Many times", "This pattern needs stronger support.")
}

enum class ContactRisk(val title: String) {
    TEXT("Text them"),
    CALL("Call them"),
    CHECK_PROFILE("Check their profile"),
    REREAD_CHATS("Reread old chats"),
    ASK_FRIENDS("Ask friends about them")
}

enum class WidgetPreference(val title: String, val description: String) {
    SMALL("Small streak widget", "A compact daily streak reminder."),
    MEDIUM("Medium streak widget", "Streak plus a short support line."),
    NONE("Not now", "Keep reminders inside the app for now.")
}

enum class DangerTimeWindow(val title: String) {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    LATE_NIGHT("Late night")
}

enum class StreakGoalDays(val days: Int, val title: String) {
    THREE(3, "3 days"),
    SEVEN(7, "7 days"),
    FOURTEEN(14, "14 days"),
    THIRTY(30, "30 days"),
    SIXTY(60, "60 days"),
    NINETY(90, "90 days")
}

@Immutable
data class OnboardingUiState(
    val currentPageIndex: Int = 0,
    val breakupDateMillis: Long = todayStartMillis(),
    val ncStartDateMillis: Long = System.currentTimeMillis(),
    val breakupType: BreakupType? = null,
    val triggerTags: Set<TriggerTag> = emptySet(),
    val reasonAnchor: String = "",
    val pledgeText: String? = null,
    val streakGoal: StreakGoalDays? = null,
    val dangerTimeWindow: DangerTimeWindow? = null,
    val checkinTime: String = "20:00",
    val baselineMood: MoodTag? = null,
    val affirmationStyle: AffirmationStyle? = null,
    val affirmationTime: String = "08:00",
    val relapseHistory: RelapseHistory? = null,
    val contactRisk: ContactRisk? = null,
    val widgetPreference: WidgetPreference? = null,
    val practiceTriggerTag: TriggerTag? = null,
    val sosPracticeCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val pageCount: Int = PageCount
    val currentStep: Int = currentPageIndex + 1
    val canGoBack: Boolean = currentPageIndex > 0
    val isLastPage: Boolean = currentPageIndex == PageCount - 1
    val progress: Float = currentStep / PageCount.toFloat()
    val canContinue: Boolean = isPageValid(currentPageIndex)
    val allInputsValid: Boolean = (0 until PageCount).all(::isPageValid)

    fun isPageValid(pageIndex: Int): Boolean {
        return when (pageIndex) {
            0 -> true
            1 -> isNotFuture(breakupDateMillis) && isNotFuture(ncStartDateMillis)
            2 -> breakupType != null
            3 -> triggerTags.isNotEmpty()
            4 -> reasonAnchor.trim().isNotEmpty()
            5 -> pledgeText != null
            6 -> streakGoal != null
            7 -> dangerTimeWindow != null && isValidTime(checkinTime)
            8 -> baselineMood != null
            9 -> affirmationStyle != null && isValidTime(affirmationTime)
            10 -> relapseHistory != null
            11 -> contactRisk != null
            12 -> widgetPreference != null
            13 -> practiceTriggerTag != null && sosPracticeCompleted
            14 -> allRequiredInputsValid()
            else -> false
        }
    }

    private fun allRequiredInputsValid(): Boolean {
        return isNotFuture(breakupDateMillis) &&
            isNotFuture(ncStartDateMillis) &&
            breakupType != null &&
            triggerTags.isNotEmpty() &&
            reasonAnchor.trim().isNotEmpty() &&
            pledgeText != null &&
            streakGoal != null &&
            dangerTimeWindow != null &&
            baselineMood != null &&
            affirmationStyle != null &&
            relapseHistory != null &&
            contactRisk != null &&
            widgetPreference != null &&
            practiceTriggerTag != null &&
            sosPracticeCompleted &&
            isValidTime(checkinTime) &&
            isValidTime(affirmationTime)
    }
}

sealed interface OnboardingAction {
    data object Next : OnboardingAction
    data object Back : OnboardingAction
    data class SetBreakupDate(val millis: Long) : OnboardingAction
    data class SetNcStartDate(val millis: Long) : OnboardingAction
    data class SelectBreakupType(val type: BreakupType) : OnboardingAction
    data class ToggleTrigger(val trigger: TriggerTag) : OnboardingAction
    data class SetReasonAnchor(val reason: String) : OnboardingAction
    data class SelectPledge(val pledge: String) : OnboardingAction
    data class SelectStreakGoal(val goal: StreakGoalDays) : OnboardingAction
    data class SelectDangerTime(val window: DangerTimeWindow) : OnboardingAction
    data class SetCheckinTime(val time: String) : OnboardingAction
    data class SelectMood(val mood: MoodTag) : OnboardingAction
    data class SelectAffirmationStyle(val style: AffirmationStyle) : OnboardingAction
    data class SetAffirmationTime(val time: String) : OnboardingAction
    data class SelectRelapseHistory(val history: RelapseHistory) : OnboardingAction
    data class SelectContactRisk(val risk: ContactRisk) : OnboardingAction
    data class SelectWidgetPreference(val preference: WidgetPreference) : OnboardingAction
    data class SelectPracticeTrigger(val trigger: TriggerTag) : OnboardingAction
    data object CompletePractice : OnboardingAction
    data object Submit : OnboardingAction
    data object ClearError : OnboardingAction
}

val ReasonAnchorSuggestions = listOf(
    "I lose myself when I go back.",
    "Peace matters more than one more reply.",
    "I need space to heal without mixed signals.",
    "The pattern keeps hurting me, and I am done feeding it."
)

val PledgeOptions = listOf(
    "Today, I protect my peace.",
    "I will pause before I reach out.",
    "I choose self-respect over impulse.",
    "I do not need contact to get closure."
)

fun OnboardingUiState.toBreakupProfileEntity(nowMillis: Long): BreakupProfileEntity {
    require(allInputsValid) { "Onboarding profile is incomplete." }
    return BreakupProfileEntity(
        breakupType = requireNotNull(breakupType).name,
        breakupDateMillis = breakupDateMillis,
        ncStartDateMillis = ncStartDateMillis,
        notifAffirmationTime = affirmationTime,
        notifCheckinTime = checkinTime,
        notifAffirmationOn = true,
        notifCheckinOn = true,
        triggerTagsCsv = triggerTags.joinToString(",") { it.name },
        reasonAnchor = reasonAnchor.trim(),
        pledgeText = requireNotNull(pledgeText),
        streakGoalDays = requireNotNull(streakGoal).days,
        dangerTimeWindow = requireNotNull(dangerTimeWindow).name,
        baselineMood = requireNotNull(baselineMood).name,
        affirmationStyle = requireNotNull(affirmationStyle).name,
        relapseHistory = requireNotNull(relapseHistory).name,
        contactRisk = requireNotNull(contactRisk).name,
        widgetPreference = requireNotNull(widgetPreference).name,
        practiceTriggerTag = requireNotNull(practiceTriggerTag).name,
        practiceCompletedAtMillis = nowMillis,
        createdAt = nowMillis
    )
}

fun todayStartMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

fun isNotFuture(millis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
    return millis <= todayEndMillis(nowMillis)
}

fun isValidTime(time: String): Boolean {
    return Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$").matches(time)
}

private fun todayEndMillis(nowMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}
