package com.appylabs.nocontact.ui.journal

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.data.JournalEntryEntity
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
    val supportingText: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun JournalRoute(
    onNewEntry: () -> Unit,
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
        onNewEntry = onNewEntry,
        onOpenEntry = onOpenEntry,
        modifier = modifier
    )
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
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = dimensions.screenPadding,
                top = dimensions.md,
                end = dimensions.screenPadding,
                bottom = dimensions.xxl * 4
            ),
            verticalArrangement = Arrangement.spacedBy(dimensions.lg)
        ) {
            item {
                JournalHeader(
                    showSearch = showSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onToggleSearch = onToggleSearch
                )
            }
            if (!showSearch) {
                item { JournalJourneyCard(uiState = uiState) }
                item { JournalEncouragementCard() }
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

        NewEntryFab(
            onClick = onNewEntry,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = dimensions.lg, bottom = dimensions.lg)
        )
    }
}

@Composable
private fun JournalHeader(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(dimensions.iconLarge + dimensions.xs)
            )
            Spacer(Modifier.width(dimensions.sm))
            Text(
                text = "NoContact",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                    contentDescription = if (showSearch) "Close search" else "Search journal",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(dimensions.iconLarge + dimensions.xs)
                )
            }
        }
        if (showSearch) {
            Spacer(Modifier.height(dimensions.sm))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search entries\u2026") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(dimensions.cardRadius)
            )
        } else {
            Spacer(Modifier.height(dimensions.lg))
            Text(
                text = "Journal",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(dimensions.xs))
            Text(
                text = "Write it out. Heal from within.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun JournalJourneyCard(uiState: JournalUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val stats = buildStats(uiState, colors)

    JournalSurface {
        Column(modifier = Modifier.padding(dimensions.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your journaling journey",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(dimensions.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.md)) {
                stats.forEach { stat ->
                    StatTile(stat = stat, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatTile(stat: JournalStat, modifier: Modifier = Modifier) {
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = modifier.height(dimensions.navHeight + dimensions.xxl + dimensions.xxl),
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(dimensions.xxs / 4, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = dimensions.xs, vertical = dimensions.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SoftTintCircle(color = stat.color, size = dimensions.xxl + dimensions.md) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(dimensions.iconLarge + dimensions.xxs)
                )
            }
            Spacer(Modifier.height(dimensions.sm))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stat.supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

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
        Box {
            JournalMountainBackdrop(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.42f)
                    .height(dimensions.navHeight + dimensions.xxl)
            )
            Row(
                modifier = Modifier.padding(dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftTintCircle(color = colors.accent, size = dimensions.navHeight) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(dimensions.iconLarge + dimensions.xs)
                    )
                }
                Spacer(Modifier.width(dimensions.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Keep going, you're doing great!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(dimensions.xxs))
                    Text(
                        text = "Your journal is a safe space for your thoughts.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalEntriesHeader(entryCount: Int) {
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (entryCount > 0) "All entries ($entryCount)" else "All entries",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun JournalEmptyState(isSearching: Boolean) {
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.xxl),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSearching) "No entries match your search." else "No entries yet.\nTap + New entry to start writing.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

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
                .padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftTintSquare(color = moodColor) {
                Icon(
                    imageVector = moodIcon,
                    contentDescription = null,
                    tint = moodColor,
                    modifier = Modifier.size(dimensions.xxl - dimensions.xxs)
                )
            }
            Spacer(Modifier.width(dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title.ifBlank { "Untitled entry" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(dimensions.xxs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buildString {
                            append(formatEntryDate(entry.createdAtMillis))
                            if (entry.mood.isNotBlank()) append("  \u2022  ${entry.mood}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.mood.isNotBlank()) {
                        Spacer(Modifier.width(dimensions.xs))
                        Box(
                            modifier = Modifier
                                .size(dimensions.xs)
                                .clip(CircleShape)
                                .background(moodColor)
                        )
                    }
                }
                if (entry.body.isNotBlank()) {
                    Spacer(Modifier.height(dimensions.xxs))
                    Text(
                        text = entry.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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

@Composable
private fun NewEntryFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(dimensions.navHeight + dimensions.xxl),
        shape = CircleShape,
        containerColor = colors.accent,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(dimensions.xxl + dimensions.xxs)
            )
            Text(
                text = "New entry",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun JournalSurface(content: @Composable () -> Unit) {
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
private fun SoftTintCircle(color: Color, size: Dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun SoftTintSquare(color: Color, content: @Composable () -> Unit) {
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = Modifier
            .size(dimensions.navHeight)
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun JournalMountainBackdrop(modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Canvas(modifier = modifier) {
        val backMountain = Path().apply {
            moveTo(size.width * 0.0f, size.height)
            lineTo(size.width * 0.46f, size.height * 0.26f)
            lineTo(size.width, size.height)
            close()
        }
        val frontMountain = Path().apply {
            moveTo(size.width * 0.16f, size.height)
            lineTo(size.width * 0.66f, size.height * 0.06f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path = backMountain, brush = Brush.verticalGradient(listOf(colors.accent.copy(alpha = 0.18f), colors.accent.copy(alpha = 0.03f))))
        drawPath(path = frontMountain, brush = Brush.verticalGradient(listOf(colors.accent.copy(alpha = 0.28f), colors.accent.copy(alpha = 0.06f))))
        drawLine(color = colors.accent.copy(alpha = 0.20f), start = Offset(size.width * 0.61f, size.height * 0.14f), end = Offset(size.width * 0.54f, size.height * 0.38f), strokeWidth = dimensions.xxs.toPx() / 2f, cap = StrokeCap.Round)
    }
}

// --- Helpers ---

@Composable
private fun buildStats(uiState: JournalUiState, colors: NoContactColorTokens): List<JournalStat> = listOf(
    JournalStat("${uiState.totalEntries}", "Entries", "Total", Icons.AutoMirrored.Rounded.MenuBook, colors.accent),
    JournalStat("${uiState.positiveDaysThisMonth}", "Positive days", "This month", Icons.Rounded.SentimentSatisfiedAlt, NoContactSuccess),
    JournalStat("${uiState.writtenDaysThisMonth}", "Written days", "This month", Icons.Rounded.Edit, NoContactFocus),
    JournalStat("${uiState.currentStreak}", "Day streak", "Current", Icons.Rounded.LocalFireDepartment, colors.accent)
)

private fun moodToColor(mood: String, colors: NoContactColorTokens): Color = when (mood) {
    "Hopeful", "Grateful", "Proud", "Happy" -> NoContactSuccess
    "Calm", "Okay" -> Color(0xFF9C5AD6)
    "Sad", "Numb" -> NoContactFocus
    "Anxious", "Angry" -> Color(0xFFE2A900)
    else -> colors.accent
}

private fun moodToIcon(mood: String): ImageVector = when (mood) {
    "Hopeful", "Grateful", "Proud", "Happy", "Okay" -> Icons.Rounded.SentimentSatisfiedAlt
    "Calm" -> Icons.Rounded.SentimentSatisfied
    "Sad" -> Icons.Rounded.SentimentDissatisfied
    "Anxious", "Numb" -> Icons.Rounded.SentimentNeutral
    "Angry" -> Icons.Rounded.SentimentVeryDissatisfied
    else -> Icons.Rounded.SentimentSatisfied
}

private fun formatEntryDate(millis: Long): String {
    val entryDay = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun Calendar.isSameDay(other: Calendar) =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    return when {
        entryDay.isSameDay(today) -> "Today, $timeStr"
        entryDay.isSameDay(yesterday) -> "Yesterday, $timeStr"
        else -> SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(millis))
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun JournalScreenPreview() {
    NoContactTheme {
        JournalScreen(
            uiState = JournalUiState(totalEntries = 4, positiveDaysThisMonth = 3, writtenDaysThisMonth = 4, currentStreak = 2),
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
