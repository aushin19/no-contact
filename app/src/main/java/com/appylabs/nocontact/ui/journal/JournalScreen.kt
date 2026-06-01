package com.appylabs.nocontact.ui.journal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.data.JournalEntryEntity
import com.appylabs.nocontact.ui.theme.IconJournalBook
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactColorTokens
import com.appylabs.nocontact.ui.theme.NoContactFocus
import com.appylabs.nocontact.ui.theme.NoContactSuccess
import com.appylabs.nocontact.ui.theme.NoContactTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class JournalStat(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun JournalRoute(
    onOpenEntry: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: JournalViewModel = viewModel(
        factory = JournalViewModel.Factory(application.repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showNewEntrySheet by remember { mutableStateOf(false) }
    var sheetSessionKey by remember { mutableIntStateOf(0) }

    val displayed = viewModel.filteredEntries(uiState.entries, searchQuery)

    JournalScreen(
        uiState = uiState,
        displayedEntries = displayed,
        searchQuery = searchQuery,
        showSearch = showSearch,
        onSearchQueryChange = { searchQuery = it },
        onToggleSearch = {
            showSearch = !showSearch
            if (!showSearch) searchQuery = ""
        },
        onNewEntry = { showNewEntrySheet = true },
        onOpenEntry = onOpenEntry,
        modifier = modifier
    )

    if (showNewEntrySheet) {
        NewEntrySheet(
            sessionKey = sheetSessionKey,
            onDismiss = {
                showNewEntrySheet = false
                sheetSessionKey++
            }
        )
    }
}

@Composable
private fun JournalScreen(
    uiState: JournalUiState,
    displayedEntries: List<JournalEntryEntity>,
    searchQuery: String,
    showSearch: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onNewEntry: () -> Unit,
    onOpenEntry: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header — same pattern as HomeHeader
            JournalHeader(
                showSearch = showSearch,
                onToggleSearch = onToggleSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.sm)
            )
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.screenPadding)
                        .padding(bottom = dimensions.sm),
                    placeholder = { Text("Search entries\u2026") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = RoundedCornerShape(dimensions.cardRadius)
                )
            }
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
                if (!showSearch) {
                    item { JournalJourneyCard(uiState = uiState) }
                    item { JournalEncouragementCard() }
                    item { Spacer(modifier = Modifier.size(dimensions.xs)) }
                }
                item { JournalEntriesHeader(entryCount = displayedEntries.size) }
                if (displayedEntries.isEmpty()) {
                    item { JournalEmptyState(isSearching = showSearch && searchQuery.isNotBlank()) }
                } else {
                    items(displayedEntries, key = { it.id }) { entry ->
                        JournalEntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                    }
                }
            }
        }
        // FAB pinned at bottom-end
        NewEntryFab(
            onClick = onNewEntry,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = dimensions.md, bottom = dimensions.xl)
        )
    }
}

// ─── Journal Header ───────────────────────────────────────────────────────────

@Composable
private fun JournalHeader(
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = IconJournalBook,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(dimensions.iconLarge)
        )
        Spacer(Modifier.width(dimensions.sm))
        Text(
            text = "Journal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggleSearch) {
            Icon(
                imageVector = if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                contentDescription = if (showSearch) "Close search" else "Search journal",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(dimensions.icon)
            )
        }
    }
}

// ─── Journaling Stats Card ────────────────────────────────────────────────────

@Composable
private fun JournalJourneyCard(uiState: JournalUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val stats = buildStats(uiState, colors)

    JournalSurface {
        Column {
            // Accent header strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(colors.accentSoft, Color.Transparent)))
                    .padding(horizontal = dimensions.md, vertical = dimensions.sm)
            ) {
                Text(
                    text = "JOURNALING STATS",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accent,
                    modifier = Modifier.semantics { heading() }
                )
            }
            // 2×2 stat grid
            Column(
                modifier = Modifier.padding(horizontal = dimensions.md, vertical = dimensions.sm),
                verticalArrangement = Arrangement.spacedBy(dimensions.sm)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                    StatTile(stat = stats[0], modifier = Modifier.weight(1f))
                    StatTile(stat = stats[1], modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                    StatTile(stat = stats[2], modifier = Modifier.weight(1f))
                    StatTile(stat = stats[3], modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatTile(stat: JournalStat, modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(dimensions.xxs / 4, colors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.sm, vertical = dimensions.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.sm)
        ) {
            // Colored icon box
            Box(
                modifier = Modifier
                    .size(dimensions.xl)
                    .clip(RoundedCornerShape(dimensions.xs))
                    .background(stat.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(dimensions.icon)
                )
            }
            // Value + label
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.xxs)) {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── Today's Reminder Card ────────────────────────────────────────────────────

@Composable
private fun JournalEncouragementCard() {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = colors.accentSoft.copy(alpha = 0.38f),
        border = BorderStroke(dimensions.xxs / 4, colors.accent.copy(alpha = 0.18f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(colors.accentSoft, Color.Transparent)))
                    .padding(horizontal = dimensions.md, vertical = dimensions.sm)
            ) {
                Text(
                    text = "TODAY'S REMINDER",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accent,
                    modifier = Modifier.semantics { heading() }
                )
            }
            Text(
                text = "Writing helps you make sense of your emotions and see your growth.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = dimensions.md,
                    end = dimensions.md,
                    bottom = dimensions.md
                )
            )
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun JournalEntriesHeader(entryCount: Int) {
    val colors = LocalNoContactColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ALL ENTRIES",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            modifier = Modifier.weight(1f)
        )
        if (entryCount > 0) {
            Text(
                text = "$entryCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun JournalEmptyState(isSearching: Boolean) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.sm)
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.xxl + dimensions.md)
                .clip(CircleShape)
                .background(colors.softIconContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconJournalBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensions.icon)
            )
        }
        Text(
            text = "No entries yet.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (isSearching) "Try a different search." else "Tap + to write your first entry.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Entry Card ───────────────────────────────────────────────────────────────

@Composable
private fun JournalEntryCard(entry: JournalEntryEntity, onClick: () -> Unit) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val moodColor = moodToColor(entry.mood, colors)
    val moodIcon = moodToIcon(entry.mood)

    JournalSurface {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = dimensions.md, vertical = dimensions.sm + dimensions.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.sm)
        ) {
            // Mood icon badge
            Box(
                modifier = Modifier
                    .size(dimensions.xl + dimensions.xs)
                    .clip(CircleShape)
                    .background(moodColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = moodIcon,
                    contentDescription = null,
                    tint = moodColor,
                    modifier = Modifier.size(dimensions.iconLarge)
                )
            }
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.xxs)
            ) {
                Text(
                    text = entry.title.ifBlank { "Untitled entry" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatEntryDate(entry.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.body.isNotBlank()) {
                    Text(
                        text = entry.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─── FAB ──────────────────────────────────────────────────────────────────────

@Composable
private fun NewEntryFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = colors.accent,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "New entry",
            modifier = Modifier.size(dimensions.iconLarge)
        )
    }
}

// ─── New Entry Bottom Sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewEntrySheet(sessionKey: Int, onDismiss: () -> Unit) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: JournalEditorViewModel = viewModel(
        key = "new_entry_sheet_$sessionKey",
        factory = JournalEditorViewModel.Factory(application.repository, entryId = null)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    val isDirty = state.title.isNotBlank() || state.body.isNotBlank()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val totalChars = state.title.trim().length + state.body.trim().length
    val canSave = totalChars >= 10

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is EditorEvent.Saved) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (isDirty) showDiscardDialog = true else onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = dimensions.screenPadding)
                .padding(bottom = dimensions.xl)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(dimensions.md)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = viewModel::save,
                    enabled = canSave && !state.isSaving,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.accent)
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Mood picker
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                Text(
                    text = "HOW ARE YOU FEELING?",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accent
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.xs)
                ) {
                    SimpleMoods.forEach { mood ->
                        val selected = state.mood == mood.label
                        MoodChip(
                            mood = mood,
                            selected = selected,
                            onClick = { viewModel.onMoodChange(if (selected) "" else mood.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Title field
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Entry title",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.cardBorder,
                    focusedBorderColor = colors.accent.copy(alpha = 0.6f),
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )

            // Body + char counter
            Column {
                OutlinedTextField(
                    value = state.body,
                    onValueChange = { if (it.length <= 1000) viewModel.onBodyChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimensions.navHeight * 4),
                    placeholder = {
                        Text(
                            text = "Write your thoughts\u2026",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = colors.accent.copy(alpha = 0.4f),
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    minLines = 4
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${state.body.length} / 1000",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Save Entry button
            Button(
                onClick = viewModel::save,
                enabled = canSave && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.pillRadius),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text(
                    text = "Save Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard entry?") },
            text = { Text("You haven't saved this entry. Discard it?") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onDismiss() }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep writing") }
            }
        )
    }
}

// ─── Shared Components ────────────────────────────────────────────────────────

@Composable
private fun JournalSurface(content: @Composable () -> Unit) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(dimensions.xxs / 4, colors.cardBorder),
        content = content
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun buildStats(uiState: JournalUiState, colors: NoContactColorTokens): List<JournalStat> = listOf(
    JournalStat("${uiState.totalEntries}", "Entries\nTotal", IconJournalBook, colors.accent),
    JournalStat("${uiState.positiveDaysThisMonth}", "Positive\nDays", Icons.Rounded.SentimentSatisfiedAlt, NoContactSuccess),
    JournalStat("${uiState.writtenDaysThisMonth}", "Days\nWritten", Icons.Rounded.Edit, NoContactFocus),
    JournalStat("${uiState.currentStreak}", "Day\nStreak", Icons.Rounded.LocalFireDepartment, colors.accent)
)

internal fun moodToColor(mood: String, colors: NoContactColorTokens): Color = when (mood) {
    "Very Good", "Good", "Hopeful", "Grateful", "Proud", "Happy" -> NoContactSuccess
    "Neutral", "Calm", "Okay" -> Color(0xFF9C5AD6)
    "Bad", "Sad", "Numb" -> NoContactFocus
    "Very Bad", "Anxious", "Angry" -> Color(0xFFE2A900)
    else -> colors.accent
}

private fun moodToIcon(mood: String): ImageVector = when (mood) {
    "Very Good", "Hopeful", "Grateful", "Proud", "Happy" -> Icons.Rounded.SentimentSatisfiedAlt
    "Good", "Calm", "Okay" -> Icons.Rounded.SentimentSatisfied
    "Neutral" -> Icons.Rounded.SentimentNeutral
    "Bad", "Sad", "Numb" -> Icons.Rounded.SentimentDissatisfied
    "Very Bad", "Anxious", "Angry" -> Icons.Rounded.SentimentVeryDissatisfied
    else -> Icons.Rounded.SentimentNeutral
}

private fun formatEntryDate(millis: Long): String {
    val entryDay = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun Calendar.isSameDay(other: Calendar) =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    return when {
        entryDay.isSameDay(today) -> "Today, $timeStr"
        entryDay.isSameDay(yesterday) -> "Yesterday, $timeStr"
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun JournalScreenPreview() {
    NoContactTheme {
        JournalScreen(
            uiState = JournalUiState(totalEntries = 47, positiveDaysThisMonth = 23, writtenDaysThisMonth = 12, currentStreak = 8),
            displayedEntries = emptyList(),
            searchQuery = "",
            showSearch = false,
            onSearchQueryChange = {},
            onToggleSearch = {},
            onNewEntry = {},
            onOpenEntry = {}
        )
    }
}
