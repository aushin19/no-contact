package com.appylabs.nocontact.ui.sos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.data.BreakupProfileEntity
import com.appylabs.nocontact.data.NoContactRepository
import com.appylabs.nocontact.data.SosSessionEntity
import com.appylabs.nocontact.data.StreakLogEntity
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun SosScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: SosViewModel = viewModel(
        factory = SosViewModel.Factory(application.repository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSlipDialog by remember { mutableStateOf(false) }
    val openedAtMillis = remember { System.currentTimeMillis() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event == SosEvent.NavigateBack) {
                onBack()
            }
        }
    }

    fun elapsedSeconds() = ((System.currentTimeMillis() - openedAtMillis) / 1000).toInt()

    SosContent(
        state = state,
        onBack = onBack,
        onEndSos = onBack,
        onResisted = { viewModel.resistedUrge(state.triggerTagsCsv, elapsedSeconds()) },
        onSlipped = { showSlipDialog = true },
        modifier = modifier
    )

    if (showSlipDialog) {
        AlertDialog(
            onDismissRequest = { showSlipDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = "No judgment here.",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Reaching out does not erase your progress. Reset the active streak only if you want a clean start.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSlipDialog = false
                        viewModel.resetStreakAfterSlip(state.triggerTagsCsv, elapsedSeconds())
                    }
                ) {
                    Text("Reset streak", color = LocalNoContactColors.current.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlipDialog = false }) {
                    Text("Keep current streak")
                }
            }
        )
    }
}

private class SosViewModel(
    private val repository: NoContactRepository
) : ViewModel() {
    val uiState: StateFlow<SosUiState> = repository.profile
        .map(::buildSosUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildSosUiState(null)
        )

    private val _events = MutableSharedFlow<SosEvent>()
    val events = _events.asSharedFlow()

    fun resistedUrge(triggerTagsCsv: String, durationSeconds: Int) {
        viewModelScope.launch {
            repository.saveSosSession(
                SosSessionEntity(
                    triggerTags = triggerTagsCsv,
                    outcome = "RESISTED",
                    durationSeconds = durationSeconds,
                    createdAt = System.currentTimeMillis()
                )
            )
            _events.emit(SosEvent.NavigateBack)
        }
    }

    fun resetStreakAfterSlip(triggerTagsCsv: String, durationSeconds: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val profile = repository.getProfileOnce()
            if (profile != null) {
                val streakStart = profile.ncStartDateMillis
                val streakDays = TimeUnit.MILLISECONDS.toDays((now - streakStart).coerceAtLeast(0L)).toInt()
                repository.saveStreakLog(
                    StreakLogEntity(
                        streakStart = streakStart,
                        streakEnd = now,
                        streakDays = streakDays,
                        reasonTag = triggerTagsCsv.split(",").firstOrNull(),
                        note = null,
                        createdAt = now
                    )
                )
            }
            repository.saveSosSession(
                SosSessionEntity(
                    triggerTags = triggerTagsCsv,
                    outcome = "RELAPSED",
                    durationSeconds = durationSeconds,
                    createdAt = now
                )
            )
            repository.updateNoContactStartDate(now)
            _events.emit(SosEvent.NavigateBack)
        }
    }

    class Factory(
        private val repository: NoContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SosViewModel::class.java)) {
                return SosViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private enum class SosEvent {
    NavigateBack
}

private data class SosUiState(
    val reasonAnchor: String,
    val pledgeText: String,
    val triggerLabels: List<String>,
    val triggerTagsCsv: String,
    val riskLabel: String,
    val dangerTimeLabel: String,
    val affirmation: String,
    val streakDays: Long
)

@Composable
private fun SosContent(
    state: SosUiState,
    onBack: () -> Unit,
    onEndSos: () -> Unit,
    onResisted: () -> Unit,
    onSlipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalNoContactDimensions.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SosTopBar(
            onBack = onBack,
            onEndSos = onEndSos,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = dimensions.xs, vertical = dimensions.xs)
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
        item {
            SosHero()
        }
        item {
            UrgeNoticeCard(state = state)
        }
        item {
            BreathingCard(state = state)
        }
        item {
            ReminderSection(state = state)
        }
        item {
            LogSection()
        }
        item {
            OutcomeSection(
                onResisted = onResisted,
                onSlipped = onSlipped
            )
        }
        item {
            BottomSupportBanner(
                text = state.affirmation,
                streakDays = state.streakDays
            )
        }
        }
    } // end Column
}

@Composable
private fun SosTopBar(
    onBack: () -> Unit,
    onEndSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        }
        Text(
            text = "SOS Mode",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        Button(
            onClick = onEndSos,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentSoft,
                contentColor = colors.accent
            ),
            contentPadding = PaddingValues(
                horizontal = dimensions.md,
                vertical = dimensions.xs
            )
        ) {
            Text(
                text = "End SOS",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SosHero() {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(dimensions.navHeight + dimensions.lg),
            shape = CircleShape,
            color = colors.accentSoft,
            shadowElevation = dimensions.xxs
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(dimensions.xxl)
                )
            }
        }
        Spacer(Modifier.height(dimensions.lg))
        Text(
            text = "You've got this.",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimensions.xs))
        Text(
            text = "Urges pass. You stay. Your future self wins.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun UrgeNoticeCard(state: SosUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    SosCard(
        container = Brush.horizontalGradient(
            listOf(colors.accentSoft.copy(alpha = 0.86f), colors.accentSoft.copy(alpha = 0.42f))
        ),
        borderColor = colors.accent.copy(alpha = 0.34f)
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(
                icon = Icons.Rounded.LocalFireDepartment,
                tint = colors.accent,
                containerColor = colors.accentSoft
            )
            Spacer(Modifier.width(dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "This urge won't last forever.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(dimensions.xxs))
                Text(
                    text = "Hardest window: ${state.dangerTimeLabel}. One minute at a time.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private enum class BreathPhase { IDLE, INHALE, HOLD, EXHALE, DONE }

@Composable
private fun rememberIsReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) < 0.5f
    }
}

@Composable
private fun BreathingCard(state: SosUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val reduceMotion = rememberIsReduceMotionEnabled()

    var phase by remember { mutableStateOf(BreathPhase.IDLE) }
    var roundsDone by remember { mutableIntStateOf(0) }
    val totalRounds = 3
    val isActive = phase != BreathPhase.IDLE && phase != BreathPhase.DONE

    // 4-7-8 phase timer — auto-advances and counts rounds
    LaunchedEffect(phase) {
        val durationMs: Long = when (phase) {
            BreathPhase.INHALE -> 4_000L
            BreathPhase.HOLD   -> 7_000L
            BreathPhase.EXHALE -> 8_000L
            else               -> return@LaunchedEffect
        }
        delay(durationMs)
        phase = when (phase) {
            BreathPhase.INHALE -> BreathPhase.HOLD
            BreathPhase.HOLD   -> BreathPhase.EXHALE
            BreathPhase.EXHALE -> {
                val next = roundsDone + 1
                roundsDone = next
                if (next < totalRounds) BreathPhase.INHALE else BreathPhase.DONE
            }
            else -> phase
        }
    }

    // Circle expands on INHALE/HOLD, contracts on EXHALE/IDLE/DONE
    val minCircle = 80.dp
    val maxCircle = 200.dp
    val targetCircle: Dp = when (phase) {
        BreathPhase.INHALE, BreathPhase.HOLD -> maxCircle
        else                                  -> minCircle
    }
    val circleSize by animateDpAsState(
        targetValue = targetCircle,
        animationSpec = if (reduceMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "breathingCircle"
    )

    val phaseLabel = when (phase) {
        BreathPhase.IDLE   -> "Breathe with me"
        BreathPhase.INHALE -> "Inhale..."
        BreathPhase.HOLD   -> "Hold..."
        BreathPhase.EXHALE -> "Exhale..."
        BreathPhase.DONE   -> "Well done"
    }
    val phaseSub = when (phase) {
        BreathPhase.IDLE -> "4 · 7 · 8  ·  $totalRounds rounds"
        BreathPhase.DONE -> "You rode it out."
        else             -> "Round ${roundsDone + 1} of $totalRounds"
    }

    SosCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionLabel("RIDE OUT THE URGE")
            Spacer(Modifier.height(dimensions.lg))

            // Fixed-height container prevents card reflow during animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(circleSize * 1.25f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    colors.accent.copy(alpha = if (isActive) 0.12f else 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Main breathing circle
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    colors.accent.copy(alpha = if (isActive) 0.45f else 0.15f),
                                    colors.accent.copy(alpha = if (isActive) 0.15f else 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (phase == BreathPhase.DONE) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircleOutline,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(dimensions.md))

            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive || phase == BreathPhase.DONE) colors.accent
                        else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.xxs))
            Text(
                text = phaseSub,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(dimensions.lg))

            Button(
                onClick = {
                    when (phase) {
                        BreathPhase.IDLE, BreathPhase.DONE -> {
                            roundsDone = 0
                            phase = BreathPhase.INHALE
                        }
                        else -> phase = BreathPhase.IDLE
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentSoft,
                    contentColor = colors.accent
                ),
                contentPadding = PaddingValues(
                    horizontal = dimensions.md,
                    vertical = dimensions.xs
                )
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Rounded.Close else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.icon)
                )
                Spacer(Modifier.width(dimensions.xs))
                Text(
                    text = when (phase) {
                        BreathPhase.IDLE -> "Start Breathing"
                        BreathPhase.DONE -> "Breathe Again"
                        else             -> "Stop"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(dimensions.sm))
            Text(
                text = "You don't have to ${state.riskLabel}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReminderSection(state: SosUiState) {
    val dimensions = LocalNoContactDimensions.current

    SosCard {
        Column(modifier = Modifier.padding(dimensions.md)) {
            SectionLabel("REMIND YOURSELF")
            Spacer(Modifier.height(dimensions.md))
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                ReminderCard(
                    title = "Reasons",
                    body = state.reasonAnchor,
                    icon = Icons.Rounded.Favorite,
                    modifier = Modifier.weight(1f)
                )
                ReminderCard(
                    title = "Affirmations",
                    body = state.pledgeText,
                    icon = Icons.Rounded.FormatQuote,
                    modifier = Modifier.weight(1f)
                )
                ReminderCard(
                    title = "Distract",
                    body = state.triggerLabels.firstOrNull() ?: "Shift your mind to something else",
                    icon = Icons.Rounded.Spa,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalNoContactDimensions.current
    val colors = LocalNoContactColors.current

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = dimensions.navHeight + dimensions.xxl)
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable { }
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(dimensions.xxs / 4, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.sm),
            verticalArrangement = Arrangement.spacedBy(dimensions.xs)
        ) {
            IconCircle(
                icon = icon,
                tint = colors.accent,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                size = dimensions.xxl + dimensions.md
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.End)
                    .size(dimensions.icon)
            )
        }
    }
}

@Composable
private fun LogSection() {
    val dimensions = LocalNoContactDimensions.current

    SosCard {
        Column(modifier = Modifier.padding(dimensions.md)) {
            SectionLabel("LOG IT (OPTIONAL)")
            Spacer(Modifier.height(dimensions.md))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cardRadius))
                    .clickable { }
                    .semantics { role = Role.Button }
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconCircle(
                    icon = Icons.Rounded.Create,
                    tint = LocalNoContactColors.current.accent,
                    containerColor = LocalNoContactColors.current.accentSoft
                )
                Spacer(Modifier.width(dimensions.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Write about what you're feeling",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Get it out. You'll feel lighter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensions.iconLarge)
                )
            }
        }
    }
}

@Composable
private fun OutcomeSection(
    onResisted: () -> Unit,
    onSlipped: () -> Unit
) {
    val dimensions = LocalNoContactDimensions.current

    SosCard {
        Column(modifier = Modifier.padding(dimensions.md)) {
            SectionLabel("AFTER THE URGE")
            Spacer(Modifier.height(dimensions.md))
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                OutcomeCard(
                    title = "I resisted",
                    body = "Proud of you. Keep going!",
                    icon = Icons.Rounded.CheckCircleOutline,
                    emphasized = true,
                    onClick = onResisted,
                    modifier = Modifier.weight(1f)
                )
                OutcomeCard(
                    title = "I slipped",
                    body = "It's okay. Reset and start again.",
                    icon = Icons.Rounded.Close,
                    emphasized = false,
                    onClick = onSlipped,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OutcomeCard(
    title: String,
    body: String,
    icon: ImageVector,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val containerColor = if (emphasized) {
        colors.accentSoft
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val tint = if (emphasized) colors.accent else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = containerColor,
        border = BorderStroke(dimensions.xxs / 4, if (emphasized) colors.accent.copy(alpha = 0.36f) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(
                icon = icon,
                tint = tint,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                size = dimensions.xxl + dimensions.md
            )
            Spacer(Modifier.width(dimensions.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = tint,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BottomSupportBanner(
    text: String,
    streakDays: Long
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    SosCard(
        container = Brush.horizontalGradient(
            listOf(colors.accentSoft.copy(alpha = 0.7f), MaterialTheme.colorScheme.surfaceContainer)
        ),
        borderColor = colors.accent.copy(alpha = 0.28f)
    ) {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(
                icon = Icons.Rounded.Favorite,
                tint = colors.accent,
                containerColor = colors.accentSoft
            )
            Spacer(Modifier.width(dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You've protected $streakDays days. This is temporary.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SosCard(
    modifier: Modifier = Modifier,
    container: Brush? = null,
    borderColor: Color = LocalNoContactColors.current.cardBorder,
    content: @Composable () -> Unit
) {
    val dimensions = LocalNoContactDimensions.current
    val shape = RoundedCornerShape(dimensions.cardRadius)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = if (container == null) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent,
        border = BorderStroke(dimensions.xxs / 4, borderColor)
    ) {
        if (container == null) {
            content()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(container)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun IconCircle(
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = LocalNoContactDimensions.current.xxl + LocalNoContactDimensions.current.lg
) {
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        }
    }
}

private fun buildSosUiState(profile: BreakupProfileEntity?): SosUiState {
    val streakDays = profile?.ncStartDateMillis?.let { start ->
        TimeUnit.MILLISECONDS.toDays((System.currentTimeMillis() - start).coerceAtLeast(0L))
    } ?: 0L
    val triggers = profile?.triggerTagsCsv
        ?.split(",")
        ?.mapNotNull(::triggerLabel)
        ?.takeIf { it.isNotEmpty() }
        ?: listOf("Late night and alone", "Replaying memories", "Want closure or answers")

    return SosUiState(
        reasonAnchor = profile?.reasonAnchor?.takeIf { it.isNotBlank() }
            ?: "Why no contact matters",
        pledgeText = profile?.pledgeText?.takeIf { it.isNotBlank() }
            ?: "Today, I protect my peace.",
        triggerLabels = triggers,
        triggerTagsCsv = profile?.triggerTagsCsv ?: "",
        riskLabel = contactRiskLabel(profile?.contactRisk),
        dangerTimeLabel = dangerTimeLabel(profile?.dangerTimeWindow),
        affirmation = affirmationFor(profile?.affirmationStyle, profile?.breakupType),
        streakDays = streakDays
    )
}

private fun triggerLabel(raw: String): String? {
    return when (raw.trim()) {
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

private fun contactRiskLabel(raw: String?): String {
    return when (raw) {
        "TEXT" -> "text them"
        "CALL" -> "call them"
        "CHECK_PROFILE" -> "check their profile"
        "REREAD_CHATS" -> "reread old chats"
        "ASK_FRIENDS" -> "ask friends about them"
        else -> "reach out"
    }
}

private fun dangerTimeLabel(raw: String?): String {
    return when (raw) {
        "MORNING" -> "morning"
        "AFTERNOON" -> "afternoon"
        "EVENING" -> "evening"
        "LATE_NIGHT" -> "late night"
        else -> "today"
    }
}

private fun affirmationFor(style: String?, breakupType: String?): String {
    // Style takes priority — tonal match to what user set during onboarding
    val byStyle = when (style) {
        "GENTLE" -> "It is okay to feel this. It does not have to become an action."
        "DIRECT" -> "Do not trade your peace for one more message."
        "TOUGH_LOVE" -> "The pattern ends when you stop feeding it. That moment is now."
        "LOGICAL" -> "An urge is a signal, not an instruction. Signals pass."
        "SPIRITUAL" -> "Release what keeps pulling you from yourself. You are already whole."
        else -> null
    }
    if (byStyle != null) return byStyle
    // Fall back to breakup-type-specific affirmation
    return when (breakupType) {
        "TOXIC" -> "Your safety matters more than one reply. Stay gone."
        "GHOSTED" -> "You do not need to chase clarity. You already have your answer."
        "MUTUAL" -> "Missing them is real. Going back will not fix the missing."
        "DIVORCE" -> "You can rebuild one honest day at a time. This is that day."
        else -> "The urge will pass. Your self-respect will remain."
    }
}


@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SosScreenPreview() {
    NoContactTheme {
        SosContent(
            state = SosUiState(
                reasonAnchor = "Why no contact matters",
                pledgeText = "Positive reminders to stay strong",
                triggerLabels = listOf("Late night and alone", "Saw their profile"),
                triggerTagsCsv = "LATE_NIGHT,PROFILE",
                riskLabel = "check their profile",
                dangerTimeLabel = "late night",
                affirmation = "You are stronger than this moment.",
                streakDays = 23
            ),
            onBack = {},
            onEndSos = {},
            onResisted = {},
            onSlipped = {}
        )
    }
}
