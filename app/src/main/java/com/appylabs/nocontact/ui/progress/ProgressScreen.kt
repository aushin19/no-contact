package com.appylabs.nocontact.ui.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.ui.theme.IconJournalBook
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactFocus
import com.appylabs.nocontact.ui.theme.NoContactSuccess

private val CHART_Y_AXIS_WIDTH = 56.dp
private val CHART_HEIGHT      = 130.dp
/** Vertical padding inside canvas so dots at level 1 and 5 aren't clipped at edges. */
private val CHART_V_PAD       = 8.dp

@Composable
fun ProgressRoute(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: ProgressViewModel = viewModel(
        factory = ProgressViewModel.Factory(application.repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProgressScreen(uiState = uiState, modifier = modifier)
}

@Composable
private fun ProgressScreen(uiState: ProgressUiState, modifier: Modifier = Modifier) {
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ProgressHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.sm)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start  = dimensions.screenPadding,
                    end    = dimensions.screenPadding,
                    top    = dimensions.xs,
                    bottom = dimensions.xxl + dimensions.md
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.sm)
            ) {
                item { StreakCard(uiState) }
                item { ThisWeekCard(uiState.weekActivity) }
                item { MoodTrendCard(uiState.moodChartData) }
                item { StatsOverviewCard(uiState) }
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun ProgressHeader(modifier: Modifier = Modifier) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(dimensions.iconLarge)
            )
            Spacer(Modifier.width(dimensions.sm))
            Text(
                text = "Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(dimensions.xxs))
        Text(
            text = "Track your healing journey.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = dimensions.iconLarge + dimensions.sm)
        )
    }
}

// ─── Shared accent-strip header ───────────────────────────────────────────────

@Composable
private fun CardAccentStrip(label: String) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(colors.accentSoft, Color.Transparent)))
            .padding(horizontal = dimensions.md, vertical = dimensions.sm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.accent
        )
    }
}

// ─── Streak Card ──────────────────────────────────────────────────────────────

@Composable
private fun StreakCard(state: ProgressUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    ProgressSurface {
        Column {
            CardAccentStrip("YOUR STREAK")
            Column(
                modifier = Modifier.padding(horizontal = dimensions.md, vertical = dimensions.sm),
                verticalArrangement = Arrangement.spacedBy(dimensions.sm)
            ) {
                // Fire badge + "X days streak"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensions.xl)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(dimensions.icon)
                        )
                    }
                    Text(
                        text = "${state.currentDays} days streak",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                }

                // Fix #1: headlineLarge (32sp) instead of displayMedium (45sp) — prevents wrapping
                Text(
                    text = state.streakDisplay,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = motivationalText(state.daysToNext),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Progress bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.xs)
                ) {
                    Text(
                        text = "${state.currentDays}d",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.accent
                    )
                    LinearProgressIndicator(
                        progress = { state.progressToNext },
                        modifier = Modifier
                            .weight(1f)
                            // Fix #8: token value only, no magic offset
                            .height(dimensions.xxs)
                            .clip(CircleShape),
                        color = colors.accent,
                        trackColor = colors.accentSoft
                    )
                    Text(
                        text = "${state.nextMilestoneDays}d",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.daysToNext > 0L) {
                    Text(
                        text = "${state.daysToNext} days to your next milestone (${state.nextMilestoneDays} days)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun motivationalText(daysToNext: Long): String = when {
    daysToNext == 0L -> "You've reached this milestone. Incredible!"
    daysToNext == 1L -> "Keep showing up for yourself. Almost there!"
    else             -> "Keep showing up for yourself. You're doing great!"
}

// ─── This Week Card ───────────────────────────────────────────────────────────

@Composable
private fun ThisWeekCard(weekActivity: List<DayActivity>) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val activeDays = weekActivity.count { it.hasEntry }

    ProgressSurface {
        Column {
            CardAccentStrip("THIS WEEK")
            Column(
                modifier = Modifier.padding(horizontal = dimensions.md, vertical = dimensions.sm),
                verticalArrangement = Arrangement.spacedBy(dimensions.md)
            ) {
                Text(
                    text = "Journal activity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekActivity.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimensions.xxs)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimensions.xl)
                                    // Fix #7: today-no-entry → outlined circle (border before clip)
                                    .then(
                                        if (day.isToday && !day.hasEntry)
                                            Modifier.border(1.5.dp, colors.accent.copy(alpha = 0.55f), CircleShape)
                                        else Modifier
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            day.hasEntry -> colors.accent
                                            day.isToday  -> Color.Transparent
                                            else         -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.hasEntry) {
                                    // Fix #10: 16dp icon inside 32dp circle (50% fill, 8dp breathing room)
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(dimensions.md)
                                    )
                                }
                            }
                            Text(
                                text = if (day.isToday) "Today" else day.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (day.isToday) colors.accent
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                val summary = when {
                    activeDays == 0 -> "No entries yet this week. Today is a great day to start!"
                    activeDays == 7 -> "Perfect week! You've journaled every single day."
                    activeDays >= 5 -> "Great job! You've written entries on $activeDays of 7 days this week."
                    else            -> "You've written entries on $activeDays of 7 days this week. Keep going!"
                }
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Mood Trend Card ──────────────────────────────────────────────────────────

@Composable
private fun MoodTrendCard(chartData: List<MoodChartPoint>) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val hasAnyMood = chartData.any { it.moodLevel > 0 }

    ProgressSurface {
        Column {
            CardAccentStrip("MOOD TREND")
            Column(
                modifier = Modifier.padding(horizontal = dimensions.md, vertical = dimensions.sm),
                verticalArrangement = Arrangement.spacedBy(dimensions.md)
            ) {
                Text(
                    text = "Your recent moods",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!hasAnyMood) {
                    Text(
                        text = "No mood data yet. Log a mood in your next journal entry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = dimensions.md)
                    )
                } else {
                    MoodLineChart(chartData = chartData, accentColor = colors.accent)
                }
                Text(
                    text = "Awareness is progress. Every mood tells a story.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MoodLineChart(chartData: List<MoodChartPoint>, accentColor: Color) {
    val gridColor    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    // Fix #4: capture surface color here — can't call MaterialTheme inside Canvas
    val surfaceColor = MaterialTheme.colorScheme.surface
    val moodLevels   = listOf("V. Good", "Good", "Neutral", "Bad", "V. Bad")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT)
        ) {
            // Fix #5: add vertical padding matching CHART_V_PAD so label centers line up with canvas grid
            Column(
                modifier = Modifier
                    .width(CHART_Y_AXIS_WIDTH)
                    .fillMaxHeight()
                    .padding(vertical = CHART_V_PAD),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                moodLevels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val count = chartData.size
                if (count < 2) return@Canvas

                // Fix #2: keep dots away from canvas edges with vertical padding
                val vertPad    = CHART_V_PAD.toPx()
                val chartRange = size.height - 2 * vertPad
                val xStep      = size.width / (count - 1).toFloat()

                // Grid lines at each of the 5 mood levels
                for (i in 0..4) {
                    val gy = vertPad + i.toFloat() / 4f * chartRange
                    drawLine(
                        color       = gridColor,
                        start       = Offset(0f, gy),
                        end         = Offset(size.width, gy),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }

                // Compute (x, y) for each point that has mood data
                val points: List<Pair<Float, Float>?> = chartData.mapIndexed { idx, pt ->
                    if (pt.moodLevel > 0) {
                        val x = idx * xStep
                        val y = vertPad + (pt.moodLevel - 1).toFloat() / 4f * chartRange
                        Pair(x, y)
                    } else null
                }

                // Lines between consecutive non-null points
                var prev: Pair<Float, Float>? = null
                points.forEach { pt ->
                    if (pt != null) {
                        prev?.let {
                            drawLine(
                                color       = accentColor,
                                start       = Offset(it.first, it.second),
                                end         = Offset(pt.first, pt.second),
                                strokeWidth = 2.dp.toPx(),
                                cap         = StrokeCap.Round
                            )
                        }
                        prev = pt
                    }
                }

                // Dots: surface outline + accent fill
                points.forEach { pt ->
                    if (pt != null) {
                        // Fix #4: use surfaceColor (adapts to dark mode) instead of Color.White
                        drawCircle(surfaceColor, radius = 5.dp.toPx(), center = Offset(pt.first, pt.second))
                        drawCircle(accentColor,  radius = 3.5.dp.toPx(), center = Offset(pt.first, pt.second))
                    }
                }
            }
        }

        // Fix #3: X-axis labels — Start/End alignment for first/last so labels hug the edge dots
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(CHART_Y_AXIS_WIDTH))
            Row(modifier = Modifier.weight(1f)) {
                chartData.forEachIndexed { index, point ->
                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = when (index) {
                            0                  -> TextAlign.Start
                            chartData.lastIndex -> TextAlign.End
                            else               -> TextAlign.Center
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─── Stats Overview Card ──────────────────────────────────────────────────────

@Composable
private fun StatsOverviewCard(state: ProgressUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val activeWritingDays = state.weekActivity.count { it.hasEntry }

    ProgressSurface {
        Column {
            CardAccentStrip("STATS OVERVIEW")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.sm, vertical = dimensions.md),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatTile(
                    icon     = IconJournalBook,
                    value    = "${state.totalJournalEntries}",
                    title    = "Journal\nEntries",
                    subtitle = "Total",
                    color    = NoContactFocus
                )
                StatTile(
                    icon     = Icons.Rounded.LocalFireDepartment,
                    value    = "${state.currentDays}",
                    title    = "No Contact",
                    subtitle = "Days",
                    color    = colors.accent
                )
                StatTile(
                    icon     = Icons.Rounded.Edit,
                    value    = "$activeWritingDays/7",
                    title    = "You Wrote",
                    subtitle = "Active Days",
                    color    = NoContactSuccess
                )
                StatTile(
                    icon     = Icons.Rounded.EmojiEvents,
                    value    = "${state.badgesEarned}",
                    title    = "Milestones",
                    subtitle = "Earned",
                    color    = Color(0xFFE2A900)
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    value: String,
    title: String,
    subtitle: String,
    color: Color
) {
    val dimensions = LocalNoContactDimensions.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.xxs)
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.xl + dimensions.xs)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        // Fix #6: maxLines=2 so "Journal\nEntries" wraps instead of truncating
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ─── Shared Surface ───────────────────────────────────────────────────────────

@Composable
private fun ProgressSurface(content: @Composable () -> Unit) {
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
