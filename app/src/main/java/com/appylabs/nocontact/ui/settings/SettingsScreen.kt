package com.appylabs.nocontact.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.data.BreakupProfileEntity
import com.appylabs.nocontact.ui.onboarding.BreakupType
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    onResetFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(application.repository, application.notificationScheduler)
    )
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pendingNotificationAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingNotificationAction?.invoke()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Notification permission denied.") }
        }
        pendingNotificationAction = null
    }

    fun withNotificationPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingNotificationAction = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Message -> scope.launch { snackbarHostState.showSnackbar(event.text) }
                SettingsEvent.ResetComplete -> onResetFinished()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SettingsScreen(
            profile = profile,
            onStartDateChange = viewModel::updateNoContactStartDate,
            onAffirmationTimeChange = viewModel::updateAffirmationTime,
            onCheckInTimeChange = viewModel::updateCheckInTime,
            onAffirmationNotificationsChange = { enabled ->
                if (enabled) {
                    withNotificationPermission { viewModel.updateAffirmationNotifications(true) }
                } else {
                    viewModel.updateAffirmationNotifications(false)
                }
            },
            onCheckInNotificationsChange = { enabled ->
                if (enabled) {
                    withNotificationPermission { viewModel.updateCheckInNotifications(true) }
                } else {
                    viewModel.updateCheckInNotifications(false)
                }
            },
            onBreakupTypeChange = viewModel::updateBreakupType,
            onDarkModeChange = { enabled -> viewModel.updateDarkMode(if (enabled) 1 else null) },
            onPlaceholder = viewModel::showPlaceholder,
            onResetAllData = viewModel::resetAllData
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(LocalNoContactDimensions.current.lg)
        )
    }
}

@Composable
fun SettingsScreen(
    profile: BreakupProfileEntity?,
    onStartDateChange: (Long) -> Unit,
    onAffirmationTimeChange: (String) -> Unit,
    onCheckInTimeChange: (String) -> Unit,
    onAffirmationNotificationsChange: (Boolean) -> Unit,
    onCheckInNotificationsChange: (Boolean) -> Unit,
    onBreakupTypeChange: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onPlaceholder: (String) -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalNoContactDimensions.current
    var showDatePicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }
    var showBreakupTypeSheet by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SettingsHeader(
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
            item {
                StreakSummaryCard(startMillis = profile?.ncStartDateMillis)
            }
            item {
                SettingsSection(title = "YOUR JOURNEY") {
                    SettingsRow(
                        icon = Icons.Rounded.FavoriteBorder,
                        title = "Breakup type",
                        subtitle = BreakupType.entries
                            .find { it.name == profile?.breakupType }?.title ?: "Not set",
                        onClick = { showBreakupTypeSheet = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "No Contact start date",
                        subtitle = profile?.ncStartDateMillis?.let(::formatDate) ?: "Not set",
                        onClick = { showDatePicker = true }
                    )
                }
            }
            item {
                SettingsSection(title = "NOTIFICATIONS") {
                    SettingsRow(
                        icon = Icons.Rounded.NotificationsNone,
                        title = "Daily affirmation",
                        subtitle = profile?.notifAffirmationTime?.let(::formatTimeLabel) ?: "Not set",
                        onClick = {
                            timePickerTarget = TimePickerTarget.Affirmation(profile?.notifAffirmationTime.orEmpty())
                        },
                        trailing = {
                            Switch(
                                checked = profile?.notifAffirmationOn == true,
                                onCheckedChange = onAffirmationNotificationsChange,
                                colors = accentSwitchColors()
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.NotificationsNone,
                        title = "Mood check-in",
                        subtitle = profile?.notifCheckinTime?.let(::formatTimeLabel) ?: "Not set",
                        onClick = {
                            timePickerTarget = TimePickerTarget.CheckIn(profile?.notifCheckinTime.orEmpty())
                        },
                        trailing = {
                            Switch(
                                checked = profile?.notifCheckinOn == true,
                                onCheckedChange = onCheckInNotificationsChange,
                                colors = accentSwitchColors()
                            )
                        }
                    )
                }
            }
            item {
                SettingsSection(title = "APPEARANCE") {
                    SettingsRow(
                        icon = Icons.Rounded.Brightness4,
                        title = "Dark mode",
                        subtitle = if (profile?.darkModeOverride == 1) "Forced on" else "Follow system",
                        trailing = {
                            Switch(
                                checked = profile?.darkModeOverride == 1,
                                onCheckedChange = onDarkModeChange,
                                colors = accentSwitchColors()
                            )
                        }
                    )
                }
            }
            item {
                SettingsSection(title = "DATA & SUPPORT") {
                    SettingsRow(
                        icon = Icons.Rounded.CloudDownload,
                        title = "Export journal",
                        subtitle = "Download entries",
                        onClick = { onPlaceholder("Journal export will be available when journal storage is added.") }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Shield,
                        title = "Privacy",
                        subtitle = "Data and perms",
                        onClick = { onPlaceholder("Your current profile is stored locally on this device.") }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        title = "Help & feedback",
                        subtitle = "Contact support",
                        onClick = { onPlaceholder("Help and feedback links are not configured yet.") }
                    )
                }
            }
            item {
                SettingsSection(title = "DANGER ZONE", isDanger = true) {
                    DangerRow(onClick = { showResetDialog = true })
                }
            }
        }
    }

    if (showDatePicker) {
        SettingsDatePickerDialog(
            value = profile?.ncStartDateMillis ?: System.currentTimeMillis(),
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                onStartDateChange(it)
                showDatePicker = false
            }
        )
    }

    timePickerTarget?.let { target ->
        SettingsTimePickerDialog(
            title = target.title,
            value = target.value,
            onDismiss = { timePickerTarget = null },
            onTimeSelected = {
                when (target) {
                    is TimePickerTarget.Affirmation -> onAffirmationTimeChange(it)
                    is TimePickerTarget.CheckIn -> onCheckInTimeChange(it)
                }
                timePickerTarget = null
            }
        )
    }

    if (showBreakupTypeSheet) {
        BreakupTypeSheet(
            current = BreakupType.entries.find { it.name == profile?.breakupType },
            onSelect = { type ->
                onBreakupTypeChange(type.name)
                showBreakupTypeSheet = false
            },
            onDismiss = { showBreakupTypeSheet = false }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all data?") },
            text = { Text("This will delete your saved profile and return you to onboarding.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetAllData()
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreakupTypeSheet(
    current: BreakupType?,
    onSelect: (BreakupType) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(
                start = dimensions.screenPadding,
                end = dimensions.screenPadding,
                bottom = dimensions.xxl + dimensions.lg
            )
        ) {
            Text(
                text = "Breakup type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(dimensions.xs))
            Text(
                text = "How did the relationship end?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(dimensions.lg))

            BreakupType.entries.forEachIndexed { index, type ->
                val selected = type == current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cardRadius))
                        .background(
                            if (selected) colors.accentSoft.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceContainer
                        )
                        .clickable { onSelect(type) }
                        .padding(horizontal = dimensions.md, vertical = dimensions.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selected) colors.accent else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(dimensions.xxs))
                        Text(
                            text = type.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (selected) {
                        Spacer(Modifier.width(dimensions.md))
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = colors.accent,
                            modifier = Modifier.size(dimensions.iconLarge)
                        )
                    }
                }
                if (index < BreakupType.entries.lastIndex) {
                    Spacer(Modifier.height(dimensions.sm))
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(dimensions.iconLarge)
            )
            Spacer(Modifier.width(dimensions.sm))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(dimensions.xxs))
        Text(
            text = "Personalize your healing journey.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = dimensions.iconLarge + dimensions.sm)
        )
    }
}

@Composable
private fun CardAccentStrip(label: String) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(colors.accentSoft, Color.Transparent)
                )
            )
            .padding(horizontal = dimensions.md, vertical = dimensions.sm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.accent
        )
    }
}

@Composable
private fun CardDangerStrip() {
    val dimensions = LocalNoContactDimensions.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = dimensions.md, vertical = dimensions.sm)
    ) {
        Text(
            text = "DANGER ZONE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun StreakSummaryCard(startMillis: Long?) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(60_000)
        }
    }

    SettingsSurface {
        Column {
            CardAccentStrip("YOUR STREAK")
            Row(
                modifier = Modifier.padding(dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftIconCircle(size = dimensions.xl + dimensions.xs) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(dimensions.iconLarge)
                    )
                }
                Spacer(Modifier.width(dimensions.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You're doing amazing!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Keep showing up.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(dimensions.sm))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = streakLabel(startMillis, nowMillis),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Current streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    isDanger: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    SettingsSurface {
        Column {
            if (isDanger) CardDangerStrip() else CardAccentStrip(title)
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val rowModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.md, vertical = dimensions.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SoftIconSquare {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(dimensions.icon)
            )
        }
        Spacer(Modifier.width(dimensions.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(dimensions.sm))
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        }
    }
}

@Composable
private fun DangerRow(onClick: () -> Unit) {
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dimensions.md, vertical = dimensions.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.xl)
                .clip(RoundedCornerShape(dimensions.xs))
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(dimensions.icon)
            )
        }
        Spacer(Modifier.width(dimensions.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Reset all data",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1
            )
            Text(
                text = "Cannot be undone",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(dimensions.sm))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(dimensions.iconLarge)
        )
    }
}

@Composable
private fun SettingsSurface(content: @Composable () -> Unit) {
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = dimensions.xxs / 4,
        border = BorderStroke(dimensions.xxs / 4, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        content = content
    )
}

@Composable
private fun SettingsDivider() {
    val dimensions = LocalNoContactDimensions.current
    HorizontalDivider(
        modifier = Modifier.padding(start = dimensions.md + dimensions.xl + dimensions.md),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    )
}

@Composable
private fun SoftIconSquare(content: @Composable () -> Unit) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = Modifier
            .size(dimensions.xl)
            .clip(RoundedCornerShape(dimensions.xs))
            .background(colors.accentSoft.copy(alpha = 0.62f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SoftIconCircle(
    size: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(LocalNoContactColors.current.accentSoft.copy(alpha = 0.62f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun accentSwitchColors(): SwitchColors {
    val colors = LocalNoContactColors.current
    return SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = colors.accent,
        checkedBorderColor = colors.accent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDatePickerDialog(
    value: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = value,
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let(onDateSelected)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTimePickerDialog(
    title: String,
    value: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val parts = remember(value) { value.split(":").mapNotNull { it.toIntOrNull() } }
    val timePickerState = rememberTimePickerState(
        initialHour = parts.getOrNull(0) ?: 8,
        initialMinute = parts.getOrNull(1) ?: 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected("%02d:%02d".format(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private sealed class TimePickerTarget(
    val title: String,
    val value: String
) {
    class Affirmation(value: String) : TimePickerTarget("Affirmation notification time", value)
    class CheckIn(value: String) : TimePickerTarget("Mood check-in reminder", value)
}

private fun streakLabel(startMillis: Long?, nowMillis: Long): String {
    if (startMillis == null) return "0d 0h 0m"
    val elapsed = (nowMillis - startMillis).coerceAtLeast(0L)
    val days = TimeUnit.MILLISECONDS.toDays(elapsed)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60
    return "${days}d ${hours}h ${minutes}m"
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis))
}

private fun formatTimeLabel(time: String): String {
    val parts = time.split(":").mapNotNull { it.toIntOrNull() }
    val hour = parts.getOrNull(0) ?: return time
    val minute = parts.getOrNull(1) ?: 0
    val amPm = if (hour >= 12) "PM" else "AM"
    val hour12 = when (val candidate = hour % 12) {
        0 -> 12
        else -> candidate
    }
    return "%d:%02d %s".format(hour12, minute, amPm)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SettingsScreenPreview() {
    NoContactTheme {
        SettingsScreen(
            profile = previewProfile(),
            onStartDateChange = {},
            onAffirmationTimeChange = {},
            onCheckInTimeChange = {},
            onAffirmationNotificationsChange = {},
            onCheckInNotificationsChange = {},
            onBreakupTypeChange = {},
            onDarkModeChange = {},
            onPlaceholder = {},
            onResetAllData = {}
        )
    }
}

private fun previewProfile(): BreakupProfileEntity {
    return BreakupProfileEntity(
        breakupType = "MUTUAL",
        breakupDateMillis = 1_742_951_600_000,
        ncStartDateMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(23) - TimeUnit.HOURS.toMillis(7) - TimeUnit.MINUTES.toMillis(42),
        notifAffirmationTime = "08:00",
        notifCheckinTime = "21:00",
        notifAffirmationOn = true,
        notifCheckinOn = true,
        triggerTagsCsv = "",
        reasonAnchor = "",
        pledgeText = "",
        streakGoalDays = 30,
        dangerTimeWindow = "",
        baselineMood = "",
        affirmationStyle = "",
        relapseHistory = "",
        contactRisk = "",
        widgetPreference = "",
        practiceTriggerTag = "",
        practiceCompletedAtMillis = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis()
    )
}
