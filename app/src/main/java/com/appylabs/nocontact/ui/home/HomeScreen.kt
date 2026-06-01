package com.appylabs.nocontact.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.data.BreakupProfileEntity
import com.appylabs.nocontact.data.JournalEntryEntity
import com.appylabs.nocontact.data.NoContactRepository
import com.appylabs.nocontact.ui.milestones.MilestoneTargets
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

// ─── Heroicons outline 24×24 ──────────────────────────────────────────────────

private fun heroIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

private val IconCheckBadge: ImageVector by lazy {
    heroIcon(
        "CheckBadge",
        "M9 12.75 11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 0 1-1.043 3.296 3.745 3.745 0 0 1-3.296 1.043A3.745 3.745 0 0 1 12 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 0 1-3.296-1.043 3.745 3.745 0 0 1-1.043-3.296A3.745 3.745 0 0 1 3 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 0 1 1.043-3.296 3.746 3.746 0 0 1 3.296-1.043A3.746 3.746 0 0 1 12 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 0 1 3.296 1.043 3.746 3.746 0 0 1 1.043 3.296A3.745 3.745 0 0 1 21 12Z"
    )
}

private val IconBookmark: ImageVector by lazy {
    heroIcon(
        "Bookmark",
        "M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0 1 11.186 0Z"
    )
}

private val IconForward: ImageVector by lazy {
    heroIcon(
        "Forward",
        "M3 8.689c0-.864.933-1.406 1.683-.977l7.108 4.061a1.125 1.125 0 0 1 0 1.954l-7.108 4.061A1.125 1.125 0 0 1 3 16.811V8.69ZM12.75 8.689c0-.864.933-1.406 1.683-.977l7.108 4.061a1.125 1.125 0 0 1 0 1.954l-7.108 4.061a1.125 1.125 0 0 1-1.683-.977V8.69Z"
    )
}

private val IconChartBar: ImageVector by lazy {
    heroIcon(
        "ChartBar",
        "M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z"
    )
}

private val IconBell: ImageVector by lazy {
    heroIcon(
        "Bell",
        "M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75V9A6 6 0 0 0 6 9v.75a8.967 8.967 0 0 1-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 0 1-5.714 0m5.714 0a3 3 0 1 1-5.714 0"
    )
}

private val IconLightBulb: ImageVector by lazy {
    heroIcon(
        "LightBulb",
        "M12 18v-5.25m0 0a6.01 6.01 0 0 0 1.5-.189m-1.5.189a6.01 6.01 0 0 1-1.5-.189m3.75 7.478a12.06 12.06 0 0 1-4.5 0m3.75 2.383a14.406 14.406 0 0 1-3 0M14.25 18v-.192c0-.983.658-1.823 1.508-2.316a7.5 7.5 0 1 0-7.517 0c.85.493 1.509 1.333 1.509 2.316V18"
    )
}

private val IconBookOpen: ImageVector by lazy {
    heroIcon(
        "BookOpen",
        "M12 6.042A8.967 8.967 0 0 0 6 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 0 1 6 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 0 1 6-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0 0 18 18a8.967 8.967 0 0 0-6 2.292m0-14.25v14.25"
    )
}

private val IconExclamationTriangle: ImageVector by lazy {
    heroIcon(
        "ExclamationTriangle",
        "M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z"
    )
}

private val IconHeart: ImageVector by lazy {
    heroIcon(
        "Heart",
        "M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z"
    )
}

// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    moodSheetTrigger: Int = 0,
    onOpenSos: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenMilestones: () -> Unit = {},
    onOpenSupport: () -> Unit = {}
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(application, application.repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showMoodSheet by remember { mutableStateOf(false) }

    // Notification permission — Android 13+ only
    var notifPermMissing by remember { mutableStateOf(!hasNotificationPermission(context)) }
    var notifBannerDismissed by remember { mutableStateOf(false) }
    var notifPermanentlyDenied by remember { mutableStateOf(false) }
    val showNotifBanner = notifPermMissing && !notifBannerDismissed && uiState.hasProfile

    // Re-check on every resume — user may have granted/revoked via system Settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notifPermMissing = !hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activity = context as? Activity
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notifPermMissing = false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity != null) {
            val canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.POST_NOTIFICATIONS
            )
            if (!canAskAgain) notifPermanentlyDenied = true
        }
    }
    var checkedMood by remember(uiState.todayKey) {
        mutableStateOf(readMoodCheckIn(context, uiState.todayKey))
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.milestoneEvents.collect { days ->
            val name = milestoneBadgeName(days)
            application.notificationScheduler.fireMilestoneNotification(days, name)
            val result = snackbarHostState.showSnackbar(
                message = "\uD83C\uDFC6 $name unlocked!",
                actionLabel = "View Badge"
            )
            if (result == SnackbarResult.ActionPerformed) {
                onOpenMilestones()
            }
        }
    }

    // Open mood sheet when arriving from a check-in notification
    LaunchedEffect(moodSheetTrigger) {
        if (moodSheetTrigger > 0) showMoodSheet = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeContent(
            state = uiState,
            checkedMood = checkedMood,
            showNotifBanner = showNotifBanner,
            notifBannerPermanentlyDenied = notifPermanentlyDenied,
            onNotifBannerAllow = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onNotifBannerOpenSettings = { openNotificationSettings(context) },
            onNotifBannerDismiss = { notifBannerDismissed = true },
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
            onJournalClick = onOpenJournal,
            onMilestoneClick = onOpenMilestones,
            onSupportClick = onOpenSupport
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
    private val application: Application,
    repository: NoContactRepository
) : ViewModel() {
    private val nowMillis = MutableStateFlow(System.currentTimeMillis())
    private val affirmationOffset = MutableStateFlow(0)
    private val savedAffirmations = MutableStateFlow<Set<String>>(emptySet())
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _milestoneEvents = MutableSharedFlow<Int>()
    val milestoneEvents: SharedFlow<Int> = _milestoneEvents.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.profile,
        nowMillis,
        affirmationOffset,
        savedAffirmations,
        repository.recentJournalEntries(2)
    ) { profile, now, offset, saved, recent ->
        buildHomeUiState(
            profile = profile,
            nowMillis = now,
            affirmationOffset = offset,
            savedAffirmations = saved,
            recentEntries = recent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = buildHomeUiState(
            profile = null,
            nowMillis = System.currentTimeMillis(),
            affirmationOffset = 0,
            savedAffirmations = emptySet(),
            recentEntries = emptyList()
        )
    )

    init {
        viewModelScope.launch {
            while (true) {
                nowMillis.value = System.currentTimeMillis()
                delay(TimeUnit.SECONDS.toMillis(1))
            }
        }
        viewModelScope.launch {
            var lastStartMillis = -1L
            var lastDays = -1L
            combine(repository.profile, nowMillis) { profile, now -> profile to now }
                .collect { (profile, now) ->
                    if (profile == null) {
                        lastDays = -1L
                        lastStartMillis = -1L
                        return@collect
                    }
                    val ncStart = profile.ncStartDateMillis
                    if (lastStartMillis > 0 && ncStart > lastStartMillis) {
                        lastDays = -1L
                        clearMilestoneNotifications()
                    }
                    lastStartMillis = ncStart
                    val currentDays = TimeUnit.MILLISECONDS.toDays((now - ncStart).coerceAtLeast(0L))
                    if (lastDays >= 0 && currentDays > lastDays) {
                        MilestoneTargets
                            .filter { it.toLong() in (lastDays + 1)..currentDays }
                            .forEach { days ->
                                if (!isMilestoneNotified(days)) {
                                    markMilestoneNotified(days)
                                    _milestoneEvents.emit(days)
                                }
                            }
                    }
                    lastDays = currentDays
                }
        }
    }

    private fun isMilestoneNotified(days: Int): Boolean =
        application.getSharedPreferences(MilestonePrefsName, Context.MODE_PRIVATE)
            .getStringSet(MilestoneNotifiedKey, emptySet())
            ?.contains(days.toString()) == true

    private fun markMilestoneNotified(days: Int) {
        val prefs = application.getSharedPreferences(MilestonePrefsName, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(MilestoneNotifiedKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(days.toString())
        prefs.edit().putStringSet(MilestoneNotifiedKey, current).apply()
    }

    fun clearMilestoneNotifications() {
        application.getSharedPreferences(MilestonePrefsName, Context.MODE_PRIVATE)
            .edit().remove(MilestoneNotifiedKey).apply()
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
        private val application: Application,
        private val repository: NoContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private const val MilestonePrefsName = "milestone_state"
private const val MilestoneNotifiedKey = "notified_days"

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
    val statsMessage: String,
    val recentEntries: List<JournalEntryEntity> = emptyList()
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

// 36 general entries — used for all users (40% of each user's daily pool)
private val GeneralAffirmations = listOf(
    "I choose peace over pain, even when it is hard.",
    "Every hour without contact is proof that I can choose myself.",
    "The urge can rise and fall without becoming an action.",
    "No contact is not punishment. It is protection.",
    "I can miss someone and still refuse the pattern that hurt me.",
    "Protecting my peace is not selfish. It is survival.",
    "I cannot heal in the same environment that hurt me.",
    "Every day without contact is a day I chose myself.",
    "The urge to reach out will pass. My self-respect will remain.",
    "I am not waiting for them. I am returning to myself.",
    "Distance is not cruelty. It is clarity.",
    "I am allowed to outgrow people who do not grow with me.",
    "My silence is a form of self-respect.",
    "The hardest part is not leaving. It is staying gone.",
    "I do not need closure from them. I can create it myself.",
    "Breaking contact is the most disciplined thing I can do today.",
    "Time is doing something right now. I just have to let it.",
    "The version of me that reaches out loses something real.",
    "Today I am choosing the version of me that heals.",
    "I can feel the pull and still not move.",
    "Progress is not always visible. Sometimes it is just surviving the urge.",
    "One more day of silence builds something I cannot yet see.",
    "Every resist is a vote for the future I actually want.",
    "I am learning to sit with discomfort instead of running back to it.",
    "The longing is real. So is my decision.",
    "Healing is not linear. Today counts anyway.",
    "I deserve someone who does not require me to beg for consistency.",
    "My nervous system is healing. I give it time.",
    "Contact feels like relief. It is not. I know this.",
    "I can hold grief and hold my ground at the same time.",
    "What felt like love and what actually was love may not be the same thing.",
    "I am not starting over. I am starting from experience.",
    "My worth does not depend on their response.",
    "Letting go is an act of faith in myself.",
    "I am the author of this next chapter.",
    "Each day I choose silence, I choose my future self."
)

// 14 entries per type — used for 60% of each user's daily pool
private val TypeAffirmations = mapOf(
    "TOXIC" to listOf(
        "I do not need to return to what made me small.",
        "Safety matters more than one more explanation.",
        "Leaving a harmful pattern is an act of self-respect.",
        "I did not deserve what happened. Leaving proves I know that.",
        "The silence I choose now is strength, not surrender.",
        "A relationship that required me to shrink was not love.",
        "I left because something was wrong, not because I gave up.",
        "Peace is not something I must earn. I can simply have it now.",
        "My instincts were right. I am honoring them.",
        "The best thing I can do for myself today is stay gone.",
        "I am not responsible for managing their feelings at the cost of mine.",
        "Anger is allowed. It does not mean I should reach out.",
        "I am safer now. That matters more than missing them.",
        "Every day away from that dynamic is a day I am rebuilding myself."
    ),
    "MUTUAL" to listOf(
        "I can honor what was real without reopening what ended.",
        "Love can be meaningful and still belong in the past.",
        "Space gives both of us room to heal.",
        "Endings are not failures. This one was honest.",
        "I can carry good memories without carrying the relationship forward.",
        "We both chose this. I respect that enough to hold the boundary.",
        "Missing them is not the same as needing to go back.",
        "Some connections are complete. That does not make them less real.",
        "It is possible to wish someone well and still keep my distance.",
        "The grief is real. So is the rightness of the decision.",
        "I chose this too. I trust that choice.",
        "Not all goodbyes need a sequel.",
        "Loving someone and letting them go can be the same act.",
        "I gave what I had. So did they. That chapter is done."
    ),
    "GHOSTED" to listOf(
        "Their silence is information. I do not need to chase clarity.",
        "Closure can come from my own decision to stop reaching.",
        "I deserve consistency, not crumbs.",
        "Someone who disappeared did not deserve my pursuit.",
        "The unanswered questions are not mine to solve alone.",
        "Chasing someone who vanished is not love. It is anxiety.",
        "I can make peace with an ending that was never explained.",
        "Their behavior reflects their capacity. Not my value.",
        "I stop reaching for someone who did not reach back.",
        "The silence told me what words never would.",
        "I deserve someone who shows up without being chased.",
        "I am not waiting for an answer that will not come. I am building instead.",
        "My peace does not require their explanation."
    ),
    "DIVORCE" to listOf(
        "A long bond ending does not mean my life is over.",
        "I can rebuild one honest day at a time.",
        "History matters, but peace matters too.",
        "Years together do not mean years of suffering is required.",
        "I gave so much. Protecting what remains is not wrong.",
        "The end of a marriage is not the end of a good life.",
        "I built a life once. I can build another.",
        "Long love does not obligate endless pain.",
        "I am allowed to grieve this and still keep moving.",
        "I am not half a person. I was whole before and I am whole now.",
        "Starting over at any age is terrifying and possible.",
        "I chose to survive this. That is enough for today.",
        "My children, my memories, my years — I carry them. They do not carry me down."
    )
)

// 1 style-keyed entry used as a tonal override in the daily pool
private val StyleAffirmations = mapOf(
    "GENTLE" to listOf("I can move slowly and still move forward."),
    "DIRECT" to listOf("Do not trade your peace for one more message."),
    "TOUGH_LOVE" to listOf("The pattern ends when I stop feeding it."),
    "LOGICAL" to listOf("An urge is a signal, not an instruction."),
    "SPIRITUAL" to listOf("I release what keeps pulling me from myself.")
)

internal fun buildHomeUiState(
    profile: BreakupProfileEntity?,
    nowMillis: Long,
    affirmationOffset: Int,
    savedAffirmations: Set<String>,
    recentEntries: List<JournalEntryEntity> = emptyList()
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
        statsMessage = "Current streak: ${streak.days} days, ${streak.hours} hours. Goal: $target days.",
        recentEntries = recentEntries
    )
}

// Builds the per-user daily pool: 60% type-matched + 40% general + 1 style entry.
internal fun buildAffirmationPool(profile: BreakupProfileEntity?): List<String> {
    val typePool = TypeAffirmations[profile?.breakupType].orEmpty()
    val stylePool = StyleAffirmations[profile?.affirmationStyle].orEmpty()
    val generalCount = if (typePool.isNotEmpty()) {
        ((typePool.size * 40 + 59) / 60).coerceAtLeast(6)
    } else {
        GeneralAffirmations.size
    }
    return typePool + GeneralAffirmations.take(generalCount) + stylePool
}

internal fun calculateStreak(startMillis: Long?, nowMillis: Long): StreakTime {
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
    showNotifBanner: Boolean = false,
    notifBannerPermanentlyDenied: Boolean = false,
    onNotifBannerAllow: () -> Unit = {},
    onNotifBannerOpenSettings: () -> Unit = {},
    onNotifBannerDismiss: () -> Unit = {},
    onNotificationsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSaveAffirmationClick: () -> Unit,
    onNextAffirmationClick: () -> Unit,
    onMoodCheckInClick: () -> Unit,
    onSosClick: () -> Unit,
    onInsightClick: () -> Unit,
    onJournalClick: () -> Unit,
    onMilestoneClick: () -> Unit,
    onSupportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dimensions = LocalNoContactDimensions.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeHeader(
            hasProfile = state.hasProfile,
            streakDays = state.streak.days,
            onNotificationsClick = onNotificationsClick,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.sm)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = dimensions.screenPadding,
                top = dimensions.xs,
                end = dimensions.screenPadding,
                bottom = dimensions.xxl + dimensions.md
            ),
            verticalArrangement = Arrangement.spacedBy(dimensions.sm)
        ) {
            if (showNotifBanner) {
                item {
                    NotificationPermissionBanner(
                        permanentlyDenied = notifBannerPermanentlyDenied,
                        onAllow = onNotifBannerAllow,
                        onOpenSettings = onNotifBannerOpenSettings,
                        onDismiss = onNotifBannerDismiss
                    )
                }
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
                HomeNavRow(
                    icon = IconBookOpen,
                    title = "Journal",
                    subtitle = "Write your thoughts",
                    onClick = onJournalClick
                )
            }
            item {
                HomeNavRow(
                    icon = IconLightBulb,
                    title = "Insights",
                    subtitle = "Find your patterns",
                    onClick = onInsightClick
                )
            }
            item {
                HomeNavRow(
                    icon = IconHeart,
                    title = "Support",
                    subtitle = "Resources and guidance",
                    onClick = onSupportClick
                )
            }
            item {
                CrisisCard(onClick = onSosClick)
            }
            item {
                HomeNavRow(
                    icon = IconChartBar,
                    title = "Your Progress",
                    subtitle = "Track your journey",
                    onClick = onMilestoneClick
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    hasProfile: Boolean,
    streakDays: Long,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(dimensions.iconLarge)
            )
            Spacer(Modifier.width(dimensions.sm))
            Text(
                text = "NoContact",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = IconBell,
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
        Text(
            text = homeGreeting(streakDays),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = dimensions.iconLarge + dimensions.sm)
        )
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
        Column(modifier = Modifier.padding(dimensions.cardPadding)) {
            // Header: label + stats chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR STREAK",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                Surface(
                    shape = CircleShape,
                    color = colors.softIconContainer,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onStatsClick)
                        .semantics { role = Role.Button }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = dimensions.sm,
                            vertical = dimensions.xxs
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.xxs)
                    ) {
                        Icon(
                            imageVector = IconChartBar,
                            contentDescription = "View stats",
                            tint = colors.accent,
                            modifier = Modifier.size(dimensions.icon)
                        )
                        Text(
                            text = "Stats",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(dimensions.sm))
            // Days — large, bold, red
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.streak.days.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Spacer(Modifier.width(dimensions.xxs))
                Text(
                    text = "days",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent,
                    modifier = Modifier.padding(bottom = dimensions.xxs)
                )
            }
            Spacer(Modifier.height(dimensions.xxs))
            // h m s secondary row
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    state.streak.hours.toString() to "h",
                    state.streak.minutes.toString() to "m",
                    state.streak.seconds.toString() to "s"
                ).forEach { (value, unit) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(dimensions.xxs))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(dimensions.sm))
            // Support line — bare row, no pill border
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.xxs)
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
            Spacer(Modifier.height(dimensions.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(dimensions.sm))
            // Calendar row — informational only
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    color = MaterialTheme.colorScheme.onSurface
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
        Column {
            // Accent header strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(colors.accentSoft, Color.Transparent)
                        )
                    )
                    .padding(horizontal = dimensions.md, vertical = dimensions.sm)
            ) {
                Text(
                    text = "TODAY'S AFFIRMATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accent,
                    modifier = Modifier.semantics { heading() }
                )
            }
            Spacer(Modifier.height(dimensions.sm))
            // Full-width quote text with embedded quotation marks
            Text(
                text = "\u201C${state.affirmation}\u201D",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.md)
            )
            Spacer(Modifier.height(dimensions.md))
            // Action bar — tonal background instead of divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = dimensions.md, vertical = dimensions.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.sm))
                        .clickable(onClick = onSaveClick)
                        .semantics { role = Role.Button }
                        .padding(vertical = dimensions.xxs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = IconBookmark,
                        contentDescription = if (state.isAffirmationSaved) "Saved" else "Save",
                        tint = if (state.isAffirmationSaved) colors.accent
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensions.icon)
                    )
                    Spacer(Modifier.width(dimensions.xxs))
                    Text(
                        text = if (state.isAffirmationSaved) "Saved" else "Save",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.isAffirmationSaved) colors.accent
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DotsIndicator(activeIndex = state.affirmationPage)
                // Next
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.sm))
                        .clickable(onClick = onNextClick)
                        .semantics { role = Role.Button }
                        .padding(vertical = dimensions.xxs),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = IconForward,
                        contentDescription = "Next affirmation",
                        tint = colors.accent,
                        modifier = Modifier.size(dimensions.icon)
                    )
                    Spacer(Modifier.width(dimensions.xxs))
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.accent
                    )
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
            Box(
                modifier = Modifier
                    .size(dimensions.xxl + dimensions.xxs)
                    .clip(RoundedCornerShape(dimensions.sm))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mood.prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = IconCheckBadge,
                contentDescription = "Check in",
                tint = colors.accent,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        }
    }
}

@Composable
private fun HomeNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    NoContactCard(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftIconCircle {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(dimensions.icon)
                )
            }
            Spacer(Modifier.width(dimensions.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CrisisCard(onClick: () -> Unit) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = colors.accentSoft,
        border = BorderStroke(dimensions.xxs / 4, colors.accent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Need immediate help?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Crisis resources and support",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = CircleShape,
                color = colors.accent
            ) {
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = dimensions.md,
                        vertical = dimensions.xs
                    )
                )
            }
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
private fun NotificationPermissionBanner(
    permanentlyDenied: Boolean,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = colors.accentSoft.copy(alpha = 0.35f),
        border = BorderStroke(dimensions.xxs / 4, colors.accent.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = IconBell,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier
                    .padding(top = dimensions.xxs)
                    .size(dimensions.iconLarge)
            )
            Spacer(Modifier.width(dimensions.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable notifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(dimensions.xxs))
                Text(
                    text = if (permanentlyDenied) {
                        "Open app settings to allow notification permission."
                    } else {
                        "Get daily affirmations and mood check-in reminders."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(dimensions.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                    Button(
                        onClick = if (permanentlyDenied) onOpenSettings else onAllow,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        contentPadding = PaddingValues(
                            horizontal = dimensions.md,
                            vertical = dimensions.xxs
                        ),
                        shape = RoundedCornerShape(dimensions.sm)
                    ) {
                        Text(
                            text = if (permanentlyDenied) "Open Settings" else "Allow",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(
                            horizontal = dimensions.sm,
                            vertical = dimensions.xxs
                        )
                    ) {
                        Text(
                            text = "Not now",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

private const val HomePrefsName = "home_screen_state"

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

private fun homeGreeting(days: Long): String {
    return when {
        days == 0L -> "You've got this. One day at a time."
        days < 7L -> "Keep going. You're building something real."
        days < 30L -> "You're finding your footing. Stay steady."
        else -> "You're proving it to yourself every day."
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

private fun milestoneBadgeName(days: Int): String = when (days) {
    1   -> "First Step"
    3   -> "Three Days"
    7   -> "One Week Strong"
    14  -> "Two Weeks Clear"
    30  -> "One Month Free"
    60  -> "Two Months Healed"
    90  -> "90 Days Warrior"
    180 -> "Six Months Strong"
    else -> "$days Days"
}

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
            onMilestoneClick = {},
            onSupportClick = {}
        )
    }
}
