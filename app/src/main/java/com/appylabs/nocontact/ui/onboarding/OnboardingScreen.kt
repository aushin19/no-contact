package com.appylabs.nocontact.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.R
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.LocalNoContactOnboardingColors
import com.appylabs.nocontact.ui.theme.NoContactTheme

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(application.repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event == OnboardingEvent.ProfileSaved) {
                onFinished()
            }
        }
    }

    BackHandler(enabled = uiState.canGoBack) {
        viewModel.onAction(OnboardingAction.Back)
    }

    OnboardingScreen(
        state = uiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
private fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingScaffold(
        state = state,
        onBack = { onAction(OnboardingAction.Back) },
        onPrimary = {
            if (state.isLastPage) {
                onAction(OnboardingAction.Submit)
            } else {
                onAction(OnboardingAction.Next)
            }
        },
        primaryLabel = when {
            state.isSaving -> "Saving..."
            state.isLastPage -> "Start Healing"
            state.currentPageIndex == 0 -> "Let's Get Started"
            else -> "Continue"
        },
        modifier = modifier
    ) {
        when (state.currentPageIndex) {
            0 -> IntroPage()
            1 -> TimelinePage(state, onAction)
            2 -> BreakupTypePage(state, onAction)
            3 -> UrgeProfilePage(state, onAction)
            4 -> ReasonAnchorPage(state, onAction)
            5 -> ContractPage(state, onAction)
            6 -> StreakGoalPage(state, onAction)
            7 -> DangerTimePage(state, onAction)
            8 -> MoodBaselinePage(state, onAction)
            9 -> AffirmationStylePage(state, onAction)
            10 -> RelapsePatternPage(state, onAction)
            11 -> ContactRiskPage(state, onAction)
            12 -> WidgetPreferencePage(state, onAction)
            13 -> SosSimulationPage(state, onAction)
            14 -> RevealPage(state)
        }
    }
}

@Composable
private fun OnboardingScaffold(
    state: OnboardingUiState,
    primaryLabel: String,
    onBack: () -> Unit,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dimensions = LocalNoContactDimensions.current
    val colors = LocalNoContactColors.current
    val onboardingColors = LocalNoContactOnboardingColors.current
    val canSubmit = if (state.isLastPage) state.allInputsValid else state.canContinue
    val isIntro = state.currentPageIndex == 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
    ) {
        OnboardingIllustrationBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = dimensions.screenPadding + dimensions.xs, vertical = dimensions.sm)
        ) {
            OnboardingProgressHeader(
                step = state.currentStep,
                count = state.pageCount,
                progress = state.progress,
                canGoBack = state.canGoBack,
                onBack = onBack
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = dimensions.xl, bottom = dimensions.lg),
                verticalArrangement = Arrangement.spacedBy(dimensions.md)
            ) {
                content()
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Button(
                onClick = onPrimary,
                enabled = canSubmit && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.navHeight)
                    .defaultMinSize(minHeight = dimensions.navHeight),
                shape = RoundedCornerShape(dimensions.pillRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White,
                    disabledContainerColor = colors.accent.copy(alpha = 0.44f),
                    disabledContentColor = Color.White.copy(alpha = 0.74f)
                )
            ) {
                Text(
                    text = primaryLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconLarge + dimensions.sm)
                )
            }
        }
    }
}

@Composable
private fun OnboardingProgressHeader(
    step: Int,
    count: Int,
    progress: Float,
    canGoBack: Boolean,
    onBack: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val onboardingColors = LocalNoContactOnboardingColors.current
    val dimensions = LocalNoContactDimensions.current

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.xxl)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.xs)
                .clip(CircleShape),
            color = colors.accent,
            trackColor = onboardingColors.panelHigh.copy(alpha = 0.62f),
            strokeCap = StrokeCap.Round
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (canGoBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(dimensions.xxl + dimensions.xs)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = onboardingColors.content
                    )
                }
            } else {
                FlameMark(Modifier.size(dimensions.xxl + dimensions.xs))
            }
            Spacer(Modifier.width(dimensions.sm))
            Text(
                text = "NoContact",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = onboardingColors.content,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$step",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " / $count",
                style = MaterialTheme.typography.headlineSmall,
                color = onboardingColors.contentMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun IntroPage() {
    IntroHeroHeader()
    PremiumFeatureList(
        items = listOf(
            FeatureItem(Icons.Rounded.Shield, "Track your progress", "See your no-contact streak in real time."),
            FeatureItem(Icons.Rounded.Warning, "SOS when urges hit", "Get through the moment without breaking."),
            FeatureItem(Icons.Rounded.Psychology, "Journal your feelings", "Write it out. Heal from within."),
            FeatureItem(Icons.Rounded.Favorite, "Celebrate milestones", "Every day counts. Every win matters.")
        )
    )
}

@Composable
private fun TimelinePage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Healing timeline",
        title = "Anchor where you are starting from.",
        body = "These dates personalize your streak and make the first progress screen feel real."
    )
    OnboardingDateRow(
        title = "Breakup date",
        value = state.breakupDateMillis,
        onDateSelected = { onAction(OnboardingAction.SetBreakupDate(it)) }
    )
    OnboardingDateRow(
        title = "No-contact start date",
        value = state.ncStartDateMillis,
        onDateSelected = { onAction(OnboardingAction.SetNcStartDate(it)) }
    )
}

@Composable
private fun BreakupTypePage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Personalization",
        title = "What happened?",
        body = "The app will use this to tune affirmations and the tone of your SOS support."
    )
    BreakupType.entries.forEach { type ->
        SelectableOnboardingCard(
            title = type.title,
            description = type.description,
            icon = Icons.Rounded.Favorite,
            selected = state.breakupType == type,
            onClick = { onAction(OnboardingAction.SelectBreakupType(type)) }
        )
    }
}

@Composable
private fun UrgeProfilePage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Urge profile",
        title = "What usually pulls you back?",
        body = "Pick the triggers that feel most familiar. Your SOS screen will meet you there."
    )
    OnboardingChipGrid {
        TriggerTag.entries.forEach { trigger ->
            PremiumFilterChip(
                text = trigger.title,
                selected = trigger in state.triggerTags,
                onClick = { onAction(OnboardingAction.ToggleTrigger(trigger)) }
            )
        }
    }
}

@Composable
private fun ReasonAnchorPage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Reason anchor",
        title = "Give your future urge one clear answer.",
        body = "When the impulse hits, this line becomes your reminder."
    )
    ReasonAnchorSuggestions.forEach { reason ->
        SelectableOnboardingCard(
            title = reason,
            description = "Use this as my anchor.",
            icon = Icons.Rounded.Shield,
            selected = state.reasonAnchor == reason,
            onClick = { onAction(OnboardingAction.SetReasonAnchor(reason)) }
        )
    }
    OutlinedTextField(
        value = state.reasonAnchor,
        onValueChange = { onAction(OnboardingAction.SetReasonAnchor(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("My reason") },
        minLines = 3,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )
}

@Composable
private fun ContractPage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Future self contract",
        title = "Make one promise before the hard moment arrives.",
        body = "This pledge becomes part of your personalized reveal."
    )
    PledgeOptions.forEach { pledge ->
        SelectableOnboardingCard(
            title = pledge,
            description = "Set this as my daily contract.",
            icon = Icons.Rounded.Check,
            selected = state.pledgeText == pledge,
            onClick = { onAction(OnboardingAction.SelectPledge(pledge)) }
        )
    }
}

@Composable
private fun StreakGoalPage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "First target",
        title = "Choose the milestone you want to protect first.",
        body = "Small goals create a visible path through the first stretch."
    )
    OnboardingChipGrid {
        StreakGoalDays.entries.forEach { goal ->
            PremiumFilterChip(
                text = goal.title,
                selected = state.streakGoal == goal,
                onClick = { onAction(OnboardingAction.SelectStreakGoal(goal)) }
            )
        }
    }
}

@Composable
private fun DangerTimePage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Danger time",
        title = "When do urges usually get loud?",
        body = "Your check-in reminder will be timed around your vulnerable window."
    )
    DangerTimeWindow.entries.forEach { window ->
        SelectableOnboardingCard(
            title = window.title,
            description = "This is when I need the most structure.",
            icon = Icons.Rounded.AccessTime,
            selected = state.dangerTimeWindow == window,
            onClick = { onAction(OnboardingAction.SelectDangerTime(window)) }
        )
    }
    OnboardingTimeRow(
        title = "Evening check-in reminder",
        value = state.checkinTime,
        onTimeSelected = { onAction(OnboardingAction.SetCheckinTime(it)) }
    )
}

@Composable
private fun MoodBaselinePage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Mood baseline",
        title = "How are you starting today?",
        body = "You do not have to feel strong to make a strong choice."
    )
    MoodTag.entries.forEach { mood ->
        SelectableOnboardingCard(
            title = mood.title,
            description = "Start from here today.",
            icon = Icons.Rounded.Favorite,
            selected = state.baselineMood == mood,
            onClick = { onAction(OnboardingAction.SelectMood(mood)) }
        )
    }
}

@Composable
private fun AffirmationStylePage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Affirmation style",
        title = "What kind of support lands best?",
        body = "Daily affirmations will use this voice first."
    )
    AffirmationStyle.entries.forEach { style ->
        SelectableOnboardingCard(
            title = style.title,
            description = style.sample,
            icon = Icons.Rounded.NotificationsNone,
            selected = state.affirmationStyle == style,
            onClick = { onAction(OnboardingAction.SelectAffirmationStyle(style)) }
        )
    }
    OnboardingTimeRow(
        title = "Daily affirmation time",
        value = state.affirmationTime,
        onTimeSelected = { onAction(OnboardingAction.SetAffirmationTime(it)) }
    )
}

@Composable
private fun RelapsePatternPage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Pattern check",
        title = "Have you tried no contact before?",
        body = "This helps the app stay shame-free if you are restarting."
    )
    RelapseHistory.entries.forEach { history ->
        SelectableOnboardingCard(
            title = history.title,
            description = history.description,
            icon = Icons.Rounded.Psychology,
            selected = state.relapseHistory == history,
            onClick = { onAction(OnboardingAction.SelectRelapseHistory(history)) }
        )
    }
}

@Composable
private fun ContactRiskPage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Contact risk",
        title = "What are you most likely to do?",
        body = "Your SOS call-to-action can mirror the exact risk you are trying to interrupt."
    )
    ContactRisk.entries.forEach { risk ->
        SelectableOnboardingCard(
            title = risk.title,
            description = "Help me pause before this happens.",
            icon = Icons.Rounded.PhoneAndroid,
            selected = state.contactRisk == risk,
            onClick = { onAction(OnboardingAction.SelectContactRisk(risk)) }
        )
    }
}

@Composable
private fun WidgetPreferencePage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    PageHeader(
        eyebrow = "Streak visibility",
        title = "How visible should your streak feel?",
        body = "This records your widget preference for the later home screen widget setup."
    )
    WidgetPreference.entries.forEach { preference ->
        SelectableOnboardingCard(
            title = preference.title,
            description = preference.description,
            icon = Icons.Rounded.PhoneAndroid,
            selected = state.widgetPreference == preference,
            onClick = { onAction(OnboardingAction.SelectWidgetPreference(preference)) }
        )
    }
}

@Composable
private fun SosSimulationPage(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    val dimensions = LocalNoContactDimensions.current
    val onboardingColors = LocalNoContactOnboardingColors.current

    PageHeader(
        eyebrow = "First SOS drill",
        title = "Practice once before the real urge hits.",
        body = "Choose a trigger, pause, and complete a short resistance drill."
    )
    OnboardingChipGrid {
        TriggerTag.entries.forEach { trigger ->
            PremiumFilterChip(
                text = trigger.title,
                selected = state.practiceTriggerTag == trigger,
                onClick = { onAction(OnboardingAction.SelectPracticeTrigger(trigger)) }
            )
        }
    }
    PremiumPanel {
        Text(
            text = "Pause drill",
            style = MaterialTheme.typography.titleLarge,
            color = onboardingColors.content,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Name the trigger. Breathe once. Remember your reason: ${state.reasonAnchor.ifBlank { "I am protecting myself." }}",
            style = MaterialTheme.typography.bodyLarge,
            color = onboardingColors.contentMuted
        )
        FilledTonalButton(
            onClick = { onAction(OnboardingAction.CompletePractice) },
            enabled = state.practiceTriggerTag != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null)
            Spacer(Modifier.width(dimensions.xs))
            Text(if (state.sosPracticeCompleted) "Resistance drill completed" else "Complete resistance drill")
        }
    }
}

@Composable
private fun RevealPage(state: OnboardingUiState) {
    PageHeader(
        eyebrow = "Your space is ready",
        title = "Your first day has a plan.",
        body = "This is the version of NoContact you are opening into."
    )
    PersonalizedRevealCard(state)
}

@Composable
private fun PageHeader(eyebrow: String, title: String, body: String) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val onboardingColors = LocalNoContactOnboardingColors.current

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.sm)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = colors.accent,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = onboardingColors.content,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = body,
            style = MaterialTheme.typography.titleLarge,
            color = onboardingColors.contentMuted
        )
    }
}

@Composable
private fun IntroHeroHeader() {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val onboardingColors = LocalNoContactOnboardingColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.lg)
    ) {
        Text(
            text = buildAnnotatedString {
                append("Protect your\n")
                withStyle(SpanStyle(color = colors.accent)) {
                    append("peace.")
                }
                append("\nRebuild\nyour life.")
            },
            style = MaterialTheme.typography.displayMedium,
            color = onboardingColors.content,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth(0.58f)
                .semantics { heading() }
        )
        Text(
            text = "NoContact helps you stay strong when the urge hits and guide you to a better you.",
            style = MaterialTheme.typography.titleLarge,
            color = onboardingColors.contentMuted,
            modifier = Modifier.fillMaxWidth(0.66f)
        )
    }
}

@Composable
private fun SelectableOnboardingCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val onboardingColors = LocalNoContactOnboardingColors.current
    val shape = MaterialTheme.shapes.large

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                this.selected = selected
                role = Role.Button
            }
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) colors.accent.copy(alpha = 0.22f) else onboardingColors.panel.copy(alpha = 0.52f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = dimensions.xxs - dimensions.xxs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = dimensions.navHeight)
                .padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(icon = icon, selected = selected)
            Spacer(Modifier.width(dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = onboardingColors.content,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = onboardingColors.contentMuted
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = colors.accent
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingChipGrid(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LocalNoContactDimensions.current.xs),
        verticalArrangement = Arrangement.spacedBy(LocalNoContactDimensions.current.xs),
        content = { content() }
    )
}

@Composable
private fun PremiumFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val onboardingColors = LocalNoContactOnboardingColors.current

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = if (selected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(LocalNoContactDimensions.current.icon)) }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = onboardingColors.panel.copy(alpha = 0.74f),
            labelColor = onboardingColors.contentMuted,
            selectedContainerColor = colors.accent.copy(alpha = 0.24f),
            selectedLabelColor = onboardingColors.content,
            selectedLeadingIconColor = colors.accent
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = onboardingColors.outline,
            selectedBorderColor = colors.accent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingDateRow(
    title: String,
    value: Long,
    onDateSelected: (Long) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    SelectableInfoRow(
        title = title,
        value = formatDate(value),
        icon = Icons.Rounded.CalendarMonth,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = value,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = isNotFuture(utcTimeMillis)
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let(onDateSelected)
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingTimeRow(
    title: String,
    value: String,
    onTimeSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val parts = remember(value) { value.split(":").mapNotNull { it.toIntOrNull() } }

    SelectableInfoRow(
        title = title,
        value = value,
        icon = Icons.Rounded.AccessTime,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = parts.getOrNull(0) ?: 8,
            initialMinute = parts.getOrNull(1) ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(top = LocalNoContactDimensions.current.xs)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected("%02d:%02d".format(timePickerState.hour, timePickerState.minute))
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SelectableInfoRow(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    SelectableOnboardingCard(
        title = title,
        description = value,
        icon = icon,
        selected = false,
        onClick = onClick
    )
}

@Composable
private fun PremiumFeatureList(items: List<FeatureItem>) {
    val dimensions = LocalNoContactDimensions.current
    val onboardingColors = LocalNoContactOnboardingColors.current

    PremiumPanel {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(icon = item.icon, selected = true)
                Spacer(Modifier.width(dimensions.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = onboardingColors.content,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = onboardingColors.contentMuted
                    )
                }
            }
            if (index != items.lastIndex) {
                HorizontalDivider(color = onboardingColors.outline.copy(alpha = 0.72f))
            }
        }
    }
}

@Composable
private fun PersonalizedRevealCard(state: OnboardingUiState) {
    val onboardingColors = LocalNoContactOnboardingColors.current

    PremiumPanel {
        RevealRow("Current streak starts", formatDate(state.ncStartDateMillis))
        RevealRow("First target", "${state.streakGoal?.days ?: 0} days")
        RevealRow("Hardest window", state.dangerTimeWindow?.title.orEmpty())
        RevealRow("SOS trigger", state.practiceTriggerTag?.title.orEmpty())
        RevealRow("Affirmation voice", state.affirmationStyle?.title.orEmpty())
        HorizontalDivider(color = onboardingColors.outline)
        Text(
            text = state.pledgeText.orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            color = onboardingColors.content,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Your reason: ${state.reasonAnchor}",
            style = MaterialTheme.typography.bodyLarge,
            color = onboardingColors.contentMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RevealRow(label: String, value: String) {
    val onboardingColors = LocalNoContactOnboardingColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = onboardingColors.contentMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = onboardingColors.content,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PremiumPanel(content: @Composable ColumnScope.() -> Unit) {
    val dimensions = LocalNoContactDimensions.current
    val onboardingColors = LocalNoContactOnboardingColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = onboardingColors.panel.copy(alpha = 0.72f),
        contentColor = onboardingColors.content,
    ) {
        Column(
            modifier = Modifier.padding(dimensions.xl),
            verticalArrangement = Arrangement.spacedBy(dimensions.lg),
            content = content
        )
    }
}

@Composable
private fun IconBubble(icon: ImageVector, selected: Boolean) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val onboardingColors = LocalNoContactOnboardingColors.current

    Surface(
        modifier = Modifier.size(dimensions.navHeight - dimensions.xs),
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.accent.copy(alpha = 0.18f) else onboardingColors.panelHigh.copy(alpha = 0.82f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colors.accent else onboardingColors.contentMuted,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        }
    }
}

@Composable
private fun OnboardingIllustrationBackdrop(modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current

    Box(modifier = modifier.background(Color(0xFF090909))) {
        Image(
            painter = painterResource(R.drawable.onboarding_illustration),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopEnd,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = LocalNoContactDimensions.current.xxl * 3)
        )
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = colors.accent.copy(alpha = 0.24f),
                radius = size.minDimension * 0.54f,
                center = Offset(size.width * 0.82f, size.height * 0.32f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF090909),
                            Color(0xEE090909),
                            Color(0x99090909),
                            Color(0x22090909)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xAA090909),
                            Color.Transparent,
                            Color(0xEE090909),
                            Color(0xFF090909)
                        )
                    )
                )
        )
    }
}

@Composable
private fun FlameMark(modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Transparent,
        contentColor = colors.accent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingIntroPreview() {
    NoContactTheme {
        OnboardingScreen(
            state = OnboardingUiState(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingFormPreview() {
    NoContactTheme {
        OnboardingScreen(
            state = OnboardingUiState(
                currentPageIndex = 2,
                breakupType = BreakupType.MUTUAL
            ),
            onAction = {}
        )
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)
