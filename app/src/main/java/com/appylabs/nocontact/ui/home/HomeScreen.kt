package com.appylabs.nocontact.ui.home

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.data.BreakupProfileEntity
import com.appylabs.nocontact.data.NoContactRepository
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenSos: () -> Unit = {}
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(application.repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showMoodSheet by remember { mutableStateOf(false) }
    var checkedMood by remember(uiState.todayKey) {
        mutableStateOf(readMoodCheckIn(context, uiState.todayKey))
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeContent(
            state = uiState,
            checkedMood = checkedMood,
            onNotificationsClick = {
                viewModel.showMessage(uiState.notificationMessage)
            },
            onStatsClick = {
                viewModel.showMessage(uiState.statsMessage)
            },
            onSaveAffirmationClick = viewModel::toggleSavedAffirmation,
            onNextAffirmationClick = viewModel::nextAffirmation,
            onMoodCheckInClick = { showMoodSheet = true },
            onSosClick = onOpenSos,
            onInsightClick = {
                viewModel.showMessage(uiState.reasonAnchor)
            },
            onJournalClick = {
                viewModel.showMessage("Journal entries will appear here after the journal database is added.")
            },
            onMilestoneClick = {
                viewModel.showMessage(uiState.milestoneMessage)
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(LocalNoContactDimensions.current.lg)
        )
    }

    if (showMoodSheet) {
        MoodCheckInSheet(
            baselineMood = uiState.baselineMood,
            onDismiss = { showMoodSheet = false },
            onMoodSelected = { mood ->
                saveMoodCheckIn(context, uiState.todayKey, mood)
                checkedMood = mood
                showMoodSheet = false
                scope.launch {
                    snackbarHostState.showSnackbar("Mood check-in saved for today.")
                }
            }
        )
    }

}

class HomeViewModel(
    repository: NoContactRepository
) : ViewModel() {
    private val nowMillis = MutableStateFlow(System.currentTimeMillis())
    private val affirmationOffset = MutableStateFlow(0)
    private val savedAffirmations = MutableStateFlow<Set<String>>(emptySet())
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.profile,
        nowMillis,
        affirmationOffset,
        savedAffirmations
    ) { profile, now, offset, saved ->
        buildHomeUiState(
            profile = profile,
            nowMillis = now,
            affirmationOffset = offset,
            savedAffirmations = saved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = buildHomeUiState(
            profile = null,
            nowMillis = System.currentTimeMillis(),
            affirmationOffset = 0,
            savedAffirmations = emptySet()
        )
    )

    init {
        viewModelScope.launch {
            while (true) {
                nowMillis.value = System.currentTimeMillis()
                delay(TimeUnit.SECONDS.toMillis(1))
            }
        }
    }

    fun nextAffirmation() {
        affirmationOffset.update { it + 1 }
    }

    fun toggleSavedAffirmation() {
        val state = uiState.value
        savedAffirmations.update { saved ->
            if (state.affirmationKey in saved) {
                saved - state.affirmationKey
            } else {
                saved + state.affirmationKey
            }
        }
    }

    fun showMessage(message: String) {
        viewModelScope.launch {
            _events.emit(message)
        }
    }

    class Factory(
        private val repository: NoContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

data class HomeUiState(
    val hasProfile: Boolean,
    val streak: StreakTime,
    val startedDate: String,
    val todayKey: String,
    val affirmation: String,
    val affirmationKey: String,
    val isAffirmationSaved: Boolean,
    val affirmationPage: Int,
    val insight: String,
    val reasonAnchor: String,
    val pledge: String,
    val supportCopy: String,
    val baselineMood: MoodOption,
    val triggerLabels: List<String>,
    val riskLabel: String,
    val dangerTimeLabel: String,
    val notificationMessage: String,
    val milestoneTargetDays: Int,
    val milestoneProgress: Float,
    val daysToMilestone: Int,
    val milestoneMessage: String,
    val statsMessage: String
)

data class StreakTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
) {
    val heroText: String = "${days}d ${hours}h ${minutes}m ${seconds}s"
}

data class MoodOption(
    val key: String,
    val label: String,
    val prompt: String
)

private val MoodOptions = listOf(
    MoodOption("DEVASTATED", "Devastated", "Heavy day. Keep this small and kind."),
    MoodOption("SAD", "Sad", "Sadness can pass without becoming a message."),
    MoodOption("NUMB", "Numb", "Numb still counts. You are still showing up."),
    MoodOption("OKAY", "Okay", "Hold onto steady. That is progress."),
    MoodOption("STRONG", "Strong", "Use that strength to protect your peace.")
)

private val GeneralAffirmations = listOf(
    "I choose peace over pain, even when it is hard.",
    "Every hour without contact is proof that I can choose myself.",
    "The urge can rise and fall without becoming an action.",
    "No contact is not punishment. It is protection.",
    "I can miss someone and still refuse the pattern that hurt me."
)

private val TypeAffirmations = mapOf(
    "TOXIC" to listOf(
        "I do not need to return to what made me small.",
        "Safety matters more than one more explanation.",
        "Leaving a harmful pattern is an act of self-respect."
    ),
    "MUTUAL" to listOf(
        "I can honor what was real without reopening what ended.",
        "Love can be meaningful and still belong in the past.",
        "Space gives both of us room to heal."
    ),
    "GHOSTED" to listOf(
        "Their silence is information. I do not need to chase clarity.",
        "Closure can come from my own decision to stop reaching.",
        "I deserve consistency, not crumbs."
    ),
    "DIVORCE" to listOf(
        "A long bond ending does not mean my life is over.",
        "I can rebuild one honest day at a time.",
        "History matters, but peace matters too."
    )
)

private val StyleAffirmations = mapOf(
    "GENTLE" to listOf("I can move slowly and still move forward."),
    "DIRECT" to listOf("Do not trade your peace for one more message."),
    "TOUGH_LOVE" to listOf("The pattern ends when I stop feeding it."),
    "LOGICAL" to listOf("An urge is a signal, not an instruction."),
    "SPIRITUAL" to listOf("I release what keeps pulling me from myself.")
)

private fun buildHomeUiState(
    profile: BreakupProfileEntity?,
    nowMillis: Long,
    affirmationOffset: Int,
    savedAffirmations: Set<String>
): HomeUiState {
    val streak = calculateStreak(profile?.ncStartDateMillis, nowMillis)
    val todayKey = formatDayKey(nowMillis)
    val affirmationPool = buildAffirmationPool(profile)
    val daySeed = dayOfYear(nowMillis)
    val index = Math.floorMod(daySeed + affirmationOffset, affirmationPool.size)
    val affirmationKey = "$todayKey-$index-${profile?.affirmationStyle.orEmpty()}"
    val target = (profile?.streakGoalDays ?: 30).coerceAtLeast(1)
    val daysForProgress = streak.days.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val progress = (daysForProgress.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val daysLeft = (target - daysForProgress).coerceAtLeast(0)
    val baselineMood = moodOption(profile?.baselineMood)
    val triggerLabels = profile?.triggerTagsCsv
        ?.split(",")
        ?.mapNotNull { triggerLabel(it) }
        ?.takeIf { it.isNotEmpty() }
        ?: listOf("Late night and alone", "Replaying memories", "Want closure or answers")

    return HomeUiState(
        hasProfile = profile != null,
        streak = streak,
        startedDate = formatDisplayDate(profile?.ncStartDateMillis),
        todayKey = todayKey,
        affirmation = affirmationPool[index],
        affirmationKey = affirmationKey,
        isAffirmationSaved = affirmationKey in savedAffirmations,
        affirmationPage = Math.floorMod(index, 3),
        insight = insightFor(profile),
        reasonAnchor = profile?.reasonAnchor?.takeIf { it.isNotBlank() }
            ?: "Protect your peace today.",
        pledge = profile?.pledgeText?.takeIf { it.isNotBlank() }
            ?: "Today, I protect my peace.",
        supportCopy = supportCopyFor(profile),
        baselineMood = baselineMood,
        triggerLabels = triggerLabels,
        riskLabel = contactRiskLabel(profile?.contactRisk),
        dangerTimeLabel = dangerTimeLabel(profile?.dangerTimeWindow),
        notificationMessage = notificationMessageFor(profile),
        milestoneTargetDays = target,
        milestoneProgress = progress,
        daysToMilestone = daysLeft,
        milestoneMessage = if (daysLeft == 0) {
            "$target day milestone reached. Keep protecting the streak."
        } else {
            "$daysLeft days until your $target day milestone."
        },
        statsMessage = "Current streak: ${streak.days} days, ${streak.hours} hours. Goal: $target days."
    )
}

private fun buildAffirmationPool(profile: BreakupProfileEntity?): List<String> {
    return GeneralAffirmations +
        TypeAffirmations[profile?.breakupType].orEmpty() +
        StyleAffirmations[profile?.affirmationStyle].orEmpty()
}

private fun calculateStreak(startMillis: Long?, nowMillis: Long): StreakTime {
    val elapsed = (nowMillis - (startMillis ?: nowMillis)).coerceAtLeast(0L)
    val days = TimeUnit.MILLISECONDS.toDays(elapsed)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % TimeUnit.DAYS.toHours(1)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % TimeUnit.MINUTES.toSeconds(1)
    return StreakTime(days, hours, minutes, seconds)
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    checkedMood: String?,
    onNotificationsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSaveAffirmationClick: () -> Unit,
    onNextAffirmationClick: () -> Unit,
    onMoodCheckInClick: () -> Unit,
    onSosClick: () -> Unit,
    onInsightClick: () -> Unit,
    onJournalClick: () -> Unit,
    onMilestoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalNoContactDimensions.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = dimensions.screenPadding,
            top = dimensions.md,
            end = dimensions.screenPadding,
            bottom = dimensions.md
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.sm)
    ) {
        item {
            HomeHeader(
                hasProfile = state.hasProfile,
                onNotificationsClick = onNotificationsClick
            )
        }
        if (!state.hasProfile) {
            item { EmptyProfileCard() }
        }
        item {
            StreakHeroCard(
                state = state,
                onStatsClick = onStatsClick
            )
        }
        item {
            AffirmationCard(
                state = state,
                onSaveClick = onSaveAffirmationClick,
                onNextClick = onNextAffirmationClick
            )
        }
        if (checkedMood == null) {
            item {
                MoodCheckInCard(
                    mood = state.baselineMood,
                    onClick = onMoodCheckInClick
                )
            }
        }
        item {
            SosCard(
                state = state,
                onClick = onSosClick
            )
        }
        item {
            InsightCard(
                text = state.insight,
                onClick = onInsightClick
            )
        }
        item { JournalEntriesCard(onClick = onJournalClick) }
        item {
            MilestoneCard(
                state = state,
                onClick = onMilestoneClick
            )
        }
    }
}

@Composable
private fun HomeHeader(
    hasProfile: Boolean,
    onNotificationsClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(dimensions.iconLarge)
        )
        Spacer(Modifier.width(dimensions.sm))
        Text(
            text = "NoContact",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Box {
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsNone,
                    contentDescription = "Notification status",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(dimensions.icon)
                )
            }
            if (hasProfile) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = dimensions.xs, end = dimensions.xs)
                        .size(dimensions.xs)
                        .clip(CircleShape)
                        .background(colors.accent)
                )
            }
        }
    }
}

@Composable
private fun EmptyProfileCard() {
    val dimensions = LocalNoContactDimensions.current

    NoContactCard {
        Column(
            modifier = Modifier.padding(dimensions.md),
            verticalArrangement = Arrangement.spacedBy(dimensions.xs)
        ) {
            Text(
                text = "Profile not found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Finish onboarding to personalize your streak, SOS plan, and daily support.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StreakHeroCard(
    state: HomeUiState,
    onStatsClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    NoContactCard(radius = dimensions.heroRadius) {
        Column(
            modifier = Modifier.padding(dimensions.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NO CONTACT STREAK",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(dimensions.xs))
            Text(
                text = state.streak.heroText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(dimensions.sm))
            Surface(
                shape = CircleShape,
                color = colors.accentSoft,
                border = BorderStroke(dimensions.xxs / 4, colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = dimensions.md, vertical = dimensions.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.xs)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(dimensions.icon)
                    )
                    Text(
                        text = streakSupportLine(state.streak.days),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(dimensions.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(dimensions.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.sm))
                    .clickable(onClick = onStatsClick)
                    .semantics { role = Role.Button }
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensions.icon)
                )
                Spacer(Modifier.width(dimensions.xs))
                Text(
                    text = "Started on ${state.startedDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "View stats",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AffirmationCard(
    state: HomeUiState,
    onSaveClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    NoContactCard {
        Box {
            MountainBackdrop(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = dimensions.lg)
                    .height(dimensions.xxl * 2)
                    .fillMaxWidth(0.5f)
            )
            Column(modifier = Modifier.padding(top = dimensions.md)) {
                Text(
                    text = "TODAY'S AFFIRMATION",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = dimensions.md)
                        .semantics { heading() }
                )
                Spacer(Modifier.height(dimensions.sm))
                Text(
                    text = "\"",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.accent,
                    modifier = Modifier.padding(horizontal = dimensions.md)
                )
                Text(
                    text = state.affirmation,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .padding(horizontal = dimensions.md)
                )
                Spacer(Modifier.height(dimensions.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.md, vertical = dimensions.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(dimensions.sm))
                            .clickable(onClick = onSaveClick)
                            .semantics { role = Role.Button },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.isAffirmationSaved) {
                                Icons.Rounded.Favorite
                            } else {
                                Icons.Rounded.FavoriteBorder
                            },
                            contentDescription = null,
                            tint = if (state.isAffirmationSaved) {
                                colors.accent
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(dimensions.icon)
                        )
                        Spacer(Modifier.width(dimensions.xs))
                        Text(
                            text = if (state.isAffirmationSaved) "Saved" else "Save",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DotsIndicator(activeIndex = state.affirmationPage)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(dimensions.sm))
                            .clickable(onClick = onNextClick)
                            .semantics { role = Role.Button },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next affirmation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodCheckInCard(
    mood: MoodOption,
    onClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    NoContactCard(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftIconCircle(size = dimensions.xxl + dimensions.xxs) {
                Icon(
                    imageVector = Icons.Rounded.SentimentSatisfiedAlt,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(dimensions.icon)
                )
            }
            Spacer(Modifier.width(dimensions.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mood check-in",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mood.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = dimensions.cardPadding, vertical = dimensions.xs)
            ) {
                Text("Check in", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun SosCard(
    state: HomeUiState,
    onClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LifeRingIcon(
                modifier = Modifier.size(dimensions.xxl + dimensions.xs),
                color = colors.accent
            )
            Spacer(Modifier.width(dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Feeling the urge?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = state.supportCopy,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(dimensions.sm),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = dimensions.lg, vertical = dimensions.xs)
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    text: String,
    onClick: () -> Unit
) {
    val dimensions = LocalNoContactDimensions.current

    NoContactCard(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
    ) {
        Column(modifier = Modifier.padding(top = dimensions.md)) {
            SectionLabel("TODAY'S INSIGHT")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftIconCircle {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = LocalNoContactColors.current.accent,
                        modifier = Modifier.size(dimensions.icon)
                    )
                }
                Spacer(Modifier.width(dimensions.cardPadding))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun JournalEntriesCard(onClick: () -> Unit) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    NoContactCard {
        Column(modifier = Modifier.padding(vertical = dimensions.md)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT JOURNAL ENTRIES",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimensions.sm))
                        .clickable(onClick = onClick)
                        .semantics { role = Role.Button }
                )
            }
            Spacer(Modifier.height(dimensions.sm))
            JournalEntryRow(
                title = "Tough day, but I stayed strong",
                meta = "Today, 9:15 PM - Hopeful",
                onClick = onClick
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = dimensions.navHeight, end = dimensions.md),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            JournalEntryRow(
                title = "Small wins are still wins",
                meta = "Yesterday, 7:45 PM - Grateful",
                onClick = onClick
            )
        }
    }
}

@Composable
private fun JournalEntryRow(
    title: String,
    meta: String,
    onClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.sm))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(horizontal = dimensions.cardPadding, vertical = dimensions.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SoftIconCircle(size = dimensions.xxl + dimensions.xs) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Article,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensions.icon)
            )
        }
        Spacer(Modifier.width(dimensions.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(dimensions.xxs))
                Box(
                    modifier = Modifier
                        .size(dimensions.xs)
                        .clip(CircleShape)
                        .background(colors.success)
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MilestoneCard(
    state: HomeUiState,
    onClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    NoContactCard(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
    ) {
        Column(modifier = Modifier.padding(dimensions.md)) {
            SectionLabel("NEXT MILESTONE")
            Spacer(Modifier.height(dimensions.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgeNumber(
                    number = state.milestoneTargetDays,
                    modifier = Modifier.size(dimensions.xxl + dimensions.xxs)
                )
                Spacer(Modifier.width(dimensions.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${state.milestoneTargetDays} Days Badge",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.daysToMilestone == 0) {
                            "Milestone reached."
                        } else {
                            "You're ${state.daysToMilestone} days away!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = state.streak.days.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.accent
                )
                Text(
                    text = " / ${state.milestoneTargetDays} days",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(dimensions.sm))
            LinearProgressIndicator(
                progress = { state.milestoneProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.xs)
                    .clip(CircleShape),
                color = colors.accent,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodCheckInSheet(
    baselineMood: MoodOption,
    onDismiss: () -> Unit,
    onMoodSelected: (String) -> Unit
) {
    val dimensions = LocalNoContactDimensions.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding)
                .padding(bottom = dimensions.lg)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(dimensions.sm)
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Your onboarding baseline was ${baselineMood.label.lowercase(Locale.getDefault())}. Check in with where you are now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                items(MoodOptions.size) { index ->
                    val mood = MoodOptions[index]
                    FilterChip(
                        selected = mood.key == baselineMood.key,
                        onClick = { onMoodSelected(mood.key) },
                        label = {
                            Text(
                                text = mood.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = if (mood.key == baselineMood.key) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimensions.icon)
                                )
                            }
                        } else {
                            null
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LocalNoContactColors.current.accentSoft,
                            selectedLabelColor = LocalNoContactColors.current.accent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NoContactCard(
    modifier: Modifier = Modifier,
    radius: Dp = LocalNoContactDimensions.current.cardRadius,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(radius)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = LocalNoContactDimensions.current.xxs - LocalNoContactDimensions.current.xxs,
        shadowElevation = LocalNoContactDimensions.current.xxs / 4,
        border = BorderStroke(LocalNoContactDimensions.current.xxs / 4, LocalNoContactColors.current.cardBorder),
        content = content
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(horizontal = LocalNoContactDimensions.current.md)
            .semantics { heading() }
    )
}

@Composable
private fun SoftIconCircle(
    modifier: Modifier = Modifier,
    size: Dp = LocalNoContactDimensions.current.xxl + LocalNoContactDimensions.current.xxs,
    content: @Composable () -> Unit
) {
    val colors = LocalNoContactColors.current

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.softIconContainer),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun DotsIndicator(activeIndex: Int) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Row(horizontalArrangement = Arrangement.spacedBy(dimensions.xxs)) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(dimensions.xs)
                    .clip(CircleShape)
                    .background(
                        if (index == activeIndex) {
                            colors.accent
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
            )
        }
    }
}

@Composable
private fun MountainBackdrop(modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current

    Canvas(modifier = modifier) {
        drawCircle(
            color = colors.accent.copy(alpha = 0.18f),
            radius = size.minDimension * 0.15f,
            center = Offset(size.width * 0.48f, size.height * 0.2f)
        )

        val backMountain = Path().apply {
            moveTo(size.width * 0.0f, size.height)
            lineTo(size.width * 0.52f, size.height * 0.38f)
            lineTo(size.width, size.height)
            close()
        }
        val frontMountain = Path().apply {
            moveTo(size.width * 0.2f, size.height)
            lineTo(size.width * 0.67f, size.height * 0.18f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(
            path = backMountain,
            brush = Brush.verticalGradient(
                listOf(colors.accent.copy(alpha = 0.14f), colors.accent.copy(alpha = 0.03f))
            )
        )
        drawPath(
            path = frontMountain,
            brush = Brush.verticalGradient(
                listOf(colors.accent.copy(alpha = 0.36f), colors.accent.copy(alpha = 0.08f))
            )
        )
    }
}

@Composable
private fun LifeRingIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val dimensions = LocalNoContactDimensions.current

    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.16f
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.34f

        drawCircle(
            color = color.copy(alpha = 0.14f),
            radius = size.minDimension / 2f
        )
        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawCircle(
            color = Color.Transparent,
            radius = radius - stroke,
            center = center
        )
        drawArc(
            color = color.copy(alpha = 0.45f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = dimensions.xxs.toPx() / 4f)
        )
    }
}

@Composable
private fun BadgeNumber(
    number: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.43f)
            val radius = size.minDimension * 0.34f
            drawCircle(
                color = colors.accentSoft,
                radius = radius + dimensions.xs.toPx(),
                center = center
            )
            drawCircle(
                color = colors.accent,
                radius = radius,
                center = center,
                style = Stroke(width = dimensions.xxs.toPx() - dimensions.xxs.toPx() / 4f)
            )
            val ribbonY = center.y + radius * 0.75f
            drawLine(
                color = colors.accent,
                start = Offset(center.x - dimensions.md.toPx(), ribbonY),
                end = Offset(center.x - (dimensions.xl + dimensions.xxs).toPx(), size.height),
                strokeWidth = dimensions.xxs.toPx() - dimensions.xxs.toPx() / 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = colors.accent,
                start = Offset(center.x + dimensions.md.toPx(), ribbonY),
                end = Offset(center.x + (dimensions.xl + dimensions.xxs).toPx(), size.height),
                strokeWidth = dimensions.xxs.toPx() - dimensions.xxs.toPx() / 4f,
                cap = StrokeCap.Round
            )
        }
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = colors.accent,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = dimensions.sm)
        )
    }
}

private fun readMoodCheckIn(context: Context, dayKey: String): String? {
    return context
        .getSharedPreferences(HomePrefsName, Context.MODE_PRIVATE)
        .getString("mood_$dayKey", null)
}

private fun saveMoodCheckIn(context: Context, dayKey: String, mood: String) {
    context
        .getSharedPreferences(HomePrefsName, Context.MODE_PRIVATE)
        .edit()
        .putString("mood_$dayKey", mood)
        .apply()
}

private fun moodOption(key: String?): MoodOption {
    return MoodOptions.firstOrNull { it.key == key } ?: MoodOptions[2]
}

private fun triggerLabel(key: String): String? {
    return when (key.trim()) {
        "LATE_NIGHT" -> "Late night and alone"
        "SONG" -> "A song reminded me"
        "PROFILE" -> "Saw their profile or story"
        "ALCOHOL" -> "Drinking or lowered guard"
        "SAD" -> "Really sad"
        "MEMORIES" -> "Replaying memories"
        "CLOSURE" -> "Want closure or answers"
        "OTHER" -> "Something else"
        else -> null
    }
}

private fun contactRiskLabel(key: String?): String {
    return when (key) {
        "TEXT" -> "texting them"
        "CALL" -> "calling them"
        "CHECK_PROFILE" -> "checking their profile"
        "REREAD_CHATS" -> "rereading old chats"
        "ASK_FRIENDS" -> "asking friends about them"
        else -> "reaching out"
    }
}

private fun dangerTimeLabel(key: String?): String {
    return when (key) {
        "MORNING" -> "morning"
        "AFTERNOON" -> "afternoon"
        "EVENING" -> "evening"
        "LATE_NIGHT" -> "late night"
        else -> "today"
    }
}

private fun insightFor(profile: BreakupProfileEntity?): String {
    return when (profile?.breakupType) {
        "TOXIC" -> "Urges are temporary. Your safety is permanent."
        "GHOSTED" -> "Silence is not a cue to chase. It is a cue to choose yourself."
        "MUTUAL" -> "Missing them does not mean the ending was wrong."
        "DIVORCE" -> "A long chapter can close without closing your future."
        else -> "Urges are temporary. Your future self is permanent."
    }
}

private fun supportCopyFor(profile: BreakupProfileEntity?): String {
    val risk = contactRiskLabel(profile?.contactRisk)
    return "Use SOS mode before $risk. Pause, reset, and protect your streak."
}

private fun notificationMessageFor(profile: BreakupProfileEntity?): String {
    if (profile == null) {
        return "Complete onboarding to set reminder times."
    }
    val affirmation = if (profile.notifAffirmationOn) {
        "affirmation at ${profile.notifAffirmationTime}"
    } else {
        "affirmation off"
    }
    val checkIn = if (profile.notifCheckinOn) {
        "check-in at ${profile.notifCheckinTime}"
    } else {
        "check-in off"
    }
    return "Reminders: $affirmation, $checkIn."
}

private fun streakSupportLine(streakDays: Long): String {
    return when (streakDays) {
        0L -> "You started today. That took courage."
        1L -> "Day 1. Every comeback starts here."
        else -> "You're stronger every second."
    }
}

private fun formatDisplayDate(millis: Long?): String {
    val value = millis ?: System.currentTimeMillis()
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(value))
}

private fun formatDayKey(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
}

private fun dayOfYear(millis: Long): Int {
    return Calendar.getInstance().apply {
        timeInMillis = millis
    }.get(Calendar.DAY_OF_YEAR)
}

private const val HomePrefsName = "home_screen_state"

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenPreview() {
    NoContactTheme {
        HomeContent(
            state = buildHomeUiState(
                profile = BreakupProfileEntity(
                    breakupType = "TOXIC",
                    breakupDateMillis = System.currentTimeMillis(),
                    ncStartDateMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(23),
                    notifAffirmationTime = "08:00",
                    notifCheckinTime = "20:00",
                    notifAffirmationOn = true,
                    notifCheckinOn = true,
                    triggerTagsCsv = "LATE_NIGHT,PROFILE,MEMORIES",
                    reasonAnchor = "Peace matters more than one more reply.",
                    pledgeText = "Today, I protect my peace.",
                    streakGoalDays = 30,
                    dangerTimeWindow = "LATE_NIGHT",
                    baselineMood = "SAD",
                    affirmationStyle = "DIRECT",
                    relapseHistory = "MANY_TIMES",
                    contactRisk = "CHECK_PROFILE",
                    widgetPreference = "MEDIUM",
                    practiceTriggerTag = "LATE_NIGHT",
                    practiceCompletedAtMillis = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis()
                ),
                nowMillis = System.currentTimeMillis(),
                affirmationOffset = 0,
                savedAffirmations = emptySet()
            ),
            checkedMood = null,
            onNotificationsClick = {},
            onStatsClick = {},
            onSaveAffirmationClick = {},
            onNextAffirmationClick = {},
            onMoodCheckInClick = {},
            onSosClick = {},
            onInsightClick = {},
            onJournalClick = {},
            onMilestoneClick = {}
        )
    }
}
