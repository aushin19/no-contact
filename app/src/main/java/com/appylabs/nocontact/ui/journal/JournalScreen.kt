package com.appylabs.nocontact.ui.journal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactFocus
import com.appylabs.nocontact.ui.theme.NoContactSuccess
import com.appylabs.nocontact.ui.theme.NoContactTheme

private data class JournalStat(
    val value: String,
    val label: String,
    val supportingText: String,
    val icon: ImageVector,
    val color: Color
)

private data class JournalEntry(
    val title: String,
    val meta: String,
    val mood: String,
    val body: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun JournalScreen(modifier: Modifier = Modifier) {
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
            item { JournalHeader() }
            item { JournalJourneyCard() }
            item { JournalEncouragementCard() }
            item { JournalEntriesHeader() }
            items(journalEntries(), key = { it.title }) { entry ->
                JournalEntryCard(entry = entry)
            }
        }

        NewEntryFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = dimensions.lg, bottom = dimensions.lg)
        )
    }
}

@Composable
private fun JournalHeader() {
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
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search journal",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(dimensions.iconLarge + dimensions.xs)
                )
            }
        }
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

@Composable
private fun JournalJourneyCard() {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

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
                Text(
                    text = "View stats",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensions.iconLarge)
                )
            }
            Spacer(Modifier.height(dimensions.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.md)) {
                journalStats().forEach { stat ->
                    StatTile(
                        stat = stat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    stat: JournalStat,
    modifier: Modifier = Modifier
) {
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
private fun JournalEntriesHeader() {
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "All entries",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensions.iconLarge)
        )
        Spacer(Modifier.width(dimensions.xs))
        Text(
            text = "Filter",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntry) {
    val dimensions = LocalNoContactDimensions.current

    JournalSurface {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftTintSquare(color = entry.color) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = entry.color,
                    modifier = Modifier.size(dimensions.xxl - dimensions.xxs)
                )
            }
            Spacer(Modifier.width(dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(dimensions.xxs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entry.meta}  •  ${entry.mood}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(dimensions.xs))
                    Box(
                        modifier = Modifier
                            .size(dimensions.xs)
                            .clip(CircleShape)
                            .background(entry.color)
                    )
                }
                Spacer(Modifier.height(dimensions.xxs))
                Text(
                    text = entry.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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

@Composable
private fun NewEntryFab(modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current

    FloatingActionButton(
        onClick = { },
        modifier = modifier.size(LocalNoContactDimensions.current.navHeight + LocalNoContactDimensions.current.xxl),
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
                modifier = Modifier.size(LocalNoContactDimensions.current.xxl + LocalNoContactDimensions.current.xxs)
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
        tonalElevation = dimensions.xxs - dimensions.xxs,
        shadowElevation = dimensions.xxs / 4,
        border = BorderStroke(dimensions.xxs / 4, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        content = content
    )
}

@Composable
private fun SoftTintCircle(
    color: Color,
    size: Dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SoftTintSquare(
    color: Color,
    content: @Composable () -> Unit
) {
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = Modifier
            .size(dimensions.navHeight)
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
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
        drawPath(
            path = backMountain,
            brush = Brush.verticalGradient(
                listOf(colors.accent.copy(alpha = 0.18f), colors.accent.copy(alpha = 0.03f))
            )
        )
        drawPath(
            path = frontMountain,
            brush = Brush.verticalGradient(
                listOf(colors.accent.copy(alpha = 0.28f), colors.accent.copy(alpha = 0.06f))
            )
        )
        drawLine(
            color = colors.accent.copy(alpha = 0.20f),
            start = Offset(size.width * 0.61f, size.height * 0.14f),
            end = Offset(size.width * 0.54f, size.height * 0.38f),
            strokeWidth = dimensions.xxs.toPx() / 2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun journalStats(): List<JournalStat> {
    val colors = LocalNoContactColors.current
    return listOf(
        JournalStat("18", "Entries", "Total", Icons.AutoMirrored.Rounded.MenuBook, colors.accent),
        JournalStat("12", "Positive days", "This month", Icons.Rounded.SentimentSatisfiedAlt, NoContactSuccess),
        JournalStat("7", "Written days", "This month", Icons.Rounded.Edit, NoContactFocus),
        JournalStat("5", "Day streak", "Current", Icons.Rounded.LocalFireDepartment, colors.accent)
    )
}

private fun journalEntries(): List<JournalEntry> {
    return listOf(
        JournalEntry(
            title = "Tough day, but I stayed strong",
            meta = "Today, 9:15 PM",
            mood = "Hopeful",
            body = "Had a rough morning thinking about everything, but I didn't break no contact. Proud of myself for choosing healing.",
            icon = Icons.Rounded.SentimentSatisfied,
            color = Color(0xFFE2A900)
        ),
        JournalEntry(
            title = "Small wins are still wins",
            meta = "Yesterday, 7:45 PM",
            mood = "Grateful",
            body = "Did my workout, ate well, and focused on work. Small steps but moving forward.",
            icon = Icons.Rounded.SentimentSatisfiedAlt,
            color = NoContactSuccess
        ),
        JournalEntry(
            title = "Missing them today",
            meta = "13 May 2025, 10:30 PM",
            mood = "Sad",
            body = "Hard to ignore the memories today, Need to remind myself why this is the right choice.",
            icon = Icons.Rounded.SentimentDissatisfied,
            color = NoContactFocus
        ),
        JournalEntry(
            title = "Peace in the silence",
            meta = "12 May 2025, 8:10 PM",
            mood = "Calm",
            body = "Enjoyed my alone time. Read, meditated and felt at peace with my decision.",
            icon = Icons.Rounded.SentimentSatisfiedAlt,
            color = Color(0xFF9C5AD6)
        )
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun JournalScreenPreview() {
    NoContactTheme {
        JournalScreen()
    }
}
