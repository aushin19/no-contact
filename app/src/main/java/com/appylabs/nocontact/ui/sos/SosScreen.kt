package com.appylabs.nocontact.ui.sos

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme
import java.util.Locale
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
    var remainingSeconds by remember { mutableIntStateOf(TotalBreathingSeconds) }
    var isBreathing by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event == SosEvent.NavigateBack) {
                onBack()
            }
        }
    }

    LaunchedEffect(isBreathing, remainingSeconds) {
        if (isBreathing && remainingSeconds > 0) {
            delay(TimeUnit.SECONDS.toMillis(1))
            remainingSeconds -= 1
        }
    }

    SosContent(
        state = state,
        remainingSeconds = remainingSeconds,
        isBreathing = isBreathing,
        onBack = onBack,
        onEndSos = onBack,
        onStartBreathing = { isBreathing = !isBreathing },
        onResisted = onBack,
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
                        viewModel.resetStreakAfterSlip()
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

    fun resetStreakAfterSlip() {
        viewModelScope.launch {
            repository.updateNoContactStartDate(System.currentTimeMillis())
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
    val riskLabel: String,
    val dangerTimeLabel: String,
    val affirmation: String,
    val streakDays: Long
)

@Composable
private fun SosContent(
    state: SosUiState,
    remainingSeconds: Int,
    isBreathing: Boolean,
    onBack: () -> Unit,
    onEndSos: () -> Unit,
    onStartBreathing: () -> Unit,
    onResisted: () -> Unit,
    onSlipped: () -> Unit,
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
            SosTopBar(
                onBack = onBack,
                onEndSos = onEndSos
            )
        }
        item {
            SosHero()
        }
        item {
            UrgeNoticeCard(state = state)
        }
        item {
            BreathingCard(
                state = state,
                remainingSeconds = remainingSeconds,
                isBreathing = isBreathing,
                onStartBreathing = onStartBreathing
            )
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
}

@Composable
private fun SosTopBar(
    onBack: () -> Unit,
    onEndSos: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun BreathingCard(
    state: SosUiState,
    remainingSeconds: Int,
    isBreathing: Boolean,
    onStartBreathing: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val elapsed = (TotalBreathingSeconds - remainingSeconds).coerceIn(0, TotalBreathingSeconds)
    val progress = elapsed / TotalBreathingSeconds.toFloat()

    SosCard {
        Column(modifier = Modifier.padding(dimensions.md)) {
            SectionLabel("RIDE OUT THE URGE")
            Spacer(Modifier.height(dimensions.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CountdownRing(
                    progress = progress,
                    timeText = formatRemaining(remainingSeconds),
                    modifier = Modifier.size(dimensions.navHeight * 2 + dimensions.xxl)
                )
                Spacer(Modifier.width(dimensions.xl))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Take 5 deep breaths",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(dimensions.sm))
                    Text(
                        text = "Focus on your breath.\nYou don't have to ${state.riskLabel}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(dimensions.md))
                    Button(
                        onClick = onStartBreathing,
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
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.icon)
                        )
                        Spacer(Modifier.width(dimensions.xs))
                        Text(
                            text = if (isBreathing) "Pause Breathing" else "Start Breathing",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
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

@Composable
private fun CountdownRing(
    progress: Float,
    timeText: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = dimensions.sm.toPx()
            val arcSize = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension / 2f
            )
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = colors.accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium,
                color = colors.accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "minutes left",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    return when (style) {
        "DIRECT" -> "You are stronger than this moment."
        "TOUGH_LOVE" -> "The pattern ends when you stop feeding it."
        "LOGICAL" -> "An urge is a signal, not an instruction."
        "SPIRITUAL" -> "Release what keeps pulling you from yourself."
        else -> when (breakupType) {
            "GHOSTED" -> "You do not need to chase clarity."
            "TOXIC" -> "Your safety matters more than one reply."
            "DIVORCE" -> "You can rebuild one honest day at a time."
            else -> "You are stronger than this moment."
        }
    }
}

private fun formatRemaining(seconds: Int): String {
    val minutesPart = seconds / TimeUnit.MINUTES.toSeconds(1).toInt()
    val secondsPart = seconds % TimeUnit.MINUTES.toSeconds(1).toInt()
    return String.format(Locale.US, "%02d:%02d", minutesPart, secondsPart)
}

private const val TotalBreathingSeconds = 5 * 60

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SosScreenPreview() {
    NoContactTheme {
        SosContent(
            state = SosUiState(
                reasonAnchor = "Why no contact matters",
                pledgeText = "Positive reminders to stay strong",
                triggerLabels = listOf("Late night and alone", "Saw their profile"),
                riskLabel = "check their profile",
                dangerTimeLabel = "late night",
                affirmation = "You are stronger than this moment.",
                streakDays = 23
            ),
            remainingSeconds = 272,
            isBreathing = false,
            onBack = {},
            onEndSos = {},
            onStartBreathing = {},
            onResisted = {},
            onSlipped = {}
        )
    }
}
