package com.appylabs.nocontact.ui.milestones

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme

private val LockedBadgeColor = Color(0xFF8A8A86)

private fun badgeColor(days: Int, achieved: Boolean, accentColor: Color): Color {
    if (!achieved) return LockedBadgeColor
    return when (days) {
        1 -> accentColor
        3 -> Color(0xFFB9642A)
        7 -> Color(0xFF73777B)
        14 -> Color(0xFFC9A227)
        30 -> Color(0xFF2471A3)
        60 -> Color(0xFF1E8449)
        90 -> Color(0xFF8E44AD)
        180 -> Color(0xFFE67E22)
        else -> accentColor
    }
}

@Composable
fun MilestonesRoute(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: MilestonesViewModel = viewModel(
        factory = MilestonesViewModel.Factory(application.repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MilestonesScreen(state = uiState, modifier = modifier)
}

@Composable
private fun MilestonesScreen(state: MilestonesUiState, modifier: Modifier = Modifier) {
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
            bottom = dimensions.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.md)
    ) {
        item { MilestonesHeader() }
        item { CurrentStreakCard(state = state) }
        item { MilestoneBadgesCard(badges = state.badges) }
        item { MilestoneHistoryCard(history = state.history) }
        item { ActionQuoteCard() }
        item { NextMilestoneCard(state = state) }
    }
}

@Composable
private fun MilestonesHeader() {
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
                    imageVector = Icons.Rounded.TrackChanges,
                    contentDescription = "Milestone goal",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(dimensions.iconLarge + dimensions.xs)
                )
            }
        }
        Spacer(Modifier.height(dimensions.lg))
        Text(
            text = "Milestones",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(dimensions.xs))
        Text(
            text = "Celebrate your progress. Every day counts.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CurrentStreakCard(state: MilestonesUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    MilestoneSurface {
        Box {
            PeakMountainArt(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = dimensions.lg, end = dimensions.lg)
                    .fillMaxWidth(0.42f)
                    .height(dimensions.xxl + dimensions.xxl + dimensions.xxl + dimensions.xl),
                dark = MaterialTheme.colorScheme.background == MaterialTheme.colorScheme.surface
            )
            Column(modifier = Modifier.padding(dimensions.md)) {
                Text(
                    text = "Current streak",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(dimensions.sm))
                Text(
                    text = state.streakDisplay,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.72f)
                )
                Spacer(Modifier.height(dimensions.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftAccentCircle(color = colors.accent, size = dimensions.xxl - dimensions.xxs) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(dimensions.icon)
                        )
                    }
                    Spacer(Modifier.width(dimensions.xs))
                    Text(
                        text = "You're doing amazing!",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.accent
                    )
                }
                Spacer(Modifier.height(dimensions.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${state.currentDays} days",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.accent,
                        modifier = Modifier.width(dimensions.xxl + dimensions.xxl + dimensions.md)
                    )
                    LinearProgressIndicator(
                        progress = { state.progressToNext },
                        modifier = Modifier
                            .weight(1f)
                            .height(dimensions.xs)
                            .clip(CircleShape),
                        color = colors.accent,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${state.nextMilestoneDays} days",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(dimensions.xxl + dimensions.xxl + dimensions.md + dimensions.xs)
                    )
                }
                Spacer(Modifier.height(dimensions.sm))
                Text(
                    text = state.motivationalMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MilestoneBadgesCard(badges: List<BadgeState>) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    MilestoneSurface {
        Column(modifier = Modifier.padding(dimensions.md)) {
            Text(
                text = "Milestone badges",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(dimensions.md))
            badges.chunked(4).forEachIndexed { index, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.md)) {
                    row.forEach { badge ->
                        val color = badgeColor(badge.days, badge.achieved, colors.accent)
                        BadgeTile(
                            badge = badge,
                            color = color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (index == 0) Spacer(Modifier.height(dimensions.md))
            }
        }
    }
}

@Composable
private fun BadgeTile(
    badge: BadgeState,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Surface(
        modifier = modifier.height(dimensions.xxl + dimensions.xxl + dimensions.xxl + dimensions.xxl + dimensions.xxs),
        shape = RoundedCornerShape(dimensions.cardRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(dimensions.xxs / 4, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = dimensions.xs, vertical = dimensions.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BadgeMedal(
                days = badge.days.toString(),
                color = color,
                achieved = badge.achieved,
                modifier = Modifier.size(dimensions.navHeight)
            )
            Spacer(Modifier.height(dimensions.xs))
            Text(
                text = badge.label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (badge.achieved) "Achieved" else "Locked",
                style = MaterialTheme.typography.bodyMedium,
                color = if (badge.achieved) colors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MilestoneHistoryCard(history: List<MilestoneHistoryState>) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    MilestoneSurface {
        Column(modifier = Modifier.padding(dimensions.md)) {
            Text(
                text = "Milestone history",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(dimensions.md))
            if (history.isEmpty()) {
                Text(
                    text = "No milestones reached yet. Keep going!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                history.forEachIndexed { index, item ->
                    val color = badgeColor(item.days, true, colors.accent)
                    HistoryRow(item = item, color = color)
                    if (index != history.lastIndex) {
                        Spacer(Modifier.height(dimensions.sm))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensions.xxs / 4)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                        )
                        Spacer(Modifier.height(dimensions.sm))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: MilestoneHistoryState, color: Color) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgeMedal(
            days = item.days.toString(),
            color = color,
            achieved = true,
            modifier = Modifier.size(dimensions.xxl + dimensions.md)
        )
        Spacer(Modifier.width(dimensions.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            shape = RoundedCornerShape(dimensions.sm),
            color = colors.accentSoft.copy(alpha = 0.55f)
        ) {
            Text(
                text = item.deltaLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.accent,
                modifier = Modifier.padding(horizontal = dimensions.sm, vertical = dimensions.xs)
            )
        }
    }
}

@Composable
private fun ActionQuoteCard() {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    MilestoneSurface {
        Box {
            PeakMountainArt(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.32f)
                    .height(dimensions.xxl + dimensions.xxl + dimensions.lg + dimensions.xxs),
                showFlag = false
            )
            Row(
                modifier = Modifier.padding(dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftAccentCircle(color = colors.accent, size = dimensions.navHeight) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(dimensions.xxl - dimensions.xxs)
                    )
                }
                Spacer(Modifier.width(dimensions.md))
                Text(
                    text = "The distance between where you are\nand where you want to be is called action.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NextMilestoneCard(state: MilestonesUiState) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val nextColor = badgeColor(state.nextMilestoneDays, false, colors.accent)

    MilestoneSurface {
        Row(
            modifier = Modifier.padding(dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeMedal(
                days = state.nextMilestoneDays.toString(),
                color = nextColor,
                achieved = false,
                modifier = Modifier.size(dimensions.navHeight + dimensions.xs)
            )
            Spacer(Modifier.width(dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Next milestone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "${state.nextMilestoneDays} Days Badge",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (state.daysToNext) {
                        0L -> "Milestone unlocked today!"
                        1L -> "Just 1 more day!"
                        else -> "You're ${state.daysToNext} days away!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(dimensions.md))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(dimensions.xxl + dimensions.xxl + dimensions.xxl + dimensions.xxl + dimensions.xxl)
            ) {
                Row {
                    Text(
                        text = "${state.currentDays}",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.accent
                    )
                    Text(
                        text = " / ${state.nextMilestoneDays} days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(dimensions.sm))
                LinearProgressIndicator(
                    progress = { state.progressToNext },
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
}

@Composable
private fun MilestoneSurface(content: @Composable () -> Unit) {
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
private fun SoftAccentCircle(color: Color, size: Dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun BadgeMedal(
    days: String,
    color: Color,
    achieved: Boolean,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalNoContactDimensions.current
    val fill = if (achieved) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f)
    val strokeColor = if (achieved) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val textColor = if (achieved) color else MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.42f)
            val radius = size.minDimension * 0.30f
            val ribbonTop = center.y + radius * 0.74f

            drawPath(
                path = Path().apply {
                    moveTo(center.x - radius * 0.58f, ribbonTop)
                    lineTo(center.x - radius * 0.94f, size.height * 0.96f)
                    lineTo(center.x - radius * 0.18f, size.height * 0.82f)
                    close()
                },
                color = strokeColor.copy(alpha = if (achieved) 0.9f else 0.55f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(center.x + radius * 0.58f, ribbonTop)
                    lineTo(center.x + radius * 0.94f, size.height * 0.96f)
                    lineTo(center.x + radius * 0.18f, size.height * 0.82f)
                    close()
                },
                color = strokeColor.copy(alpha = if (achieved) 0.9f else 0.55f)
            )

            repeat(18) { index ->
                val angle = Math.toRadians((index * 20).toDouble())
                val outer = radius * if (index % 2 == 0) 1.18f else 1.03f
                val inner = radius * 0.93f
                val start = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * inner,
                    y = center.y + kotlin.math.sin(angle).toFloat() * inner
                )
                val end = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * outer,
                    y = center.y + kotlin.math.sin(angle).toFloat() * outer
                )
                drawLine(
                    color = strokeColor.copy(alpha = 0.8f),
                    start = start,
                    end = end,
                    strokeWidth = dimensions.xxs.toPx() / 2f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(color = fill, radius = radius * 1.08f, center = center)
            drawCircle(
                color = strokeColor,
                radius = radius,
                center = center,
                style = Stroke(width = dimensions.xxs.toPx() / 2f)
            )
            drawCircle(
                color = strokeColor.copy(alpha = 0.5f),
                radius = radius * 0.78f,
                center = center,
                style = Stroke(width = dimensions.xxs.toPx() / 4f)
            )
        }
        Text(
            text = days,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = dimensions.sm)
        )
    }
}

@Composable
private fun PeakMountainArt(
    modifier: Modifier = Modifier,
    showFlag: Boolean = true,
    dark: Boolean = false
) {
    val dimensions = LocalNoContactDimensions.current
    val colors = LocalNoContactColors.current
    val peak = if (dark) colors.accent.copy(alpha = 0.78f) else colors.accent.copy(alpha = 0.28f)
    val base = if (dark) colors.accent.copy(alpha = 0.18f) else colors.accent.copy(alpha = 0.08f)

    Canvas(modifier = modifier) {
        val backMountain = Path().apply {
            moveTo(size.width * 0.02f, size.height)
            lineTo(size.width * 0.55f, size.height * 0.20f)
            lineTo(size.width, size.height)
            close()
        }
        val frontMountain = Path().apply {
            moveTo(size.width * 0.18f, size.height)
            lineTo(size.width * 0.66f, size.height * 0.02f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(
            path = backMountain,
            brush = Brush.verticalGradient(listOf(base, base.copy(alpha = 0.02f)))
        )
        drawPath(
            path = frontMountain,
            brush = Brush.verticalGradient(listOf(peak, peak.copy(alpha = 0.05f)))
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.62f, size.height * 0.10f)
                cubicTo(
                    size.width * 0.72f, size.height * 0.36f,
                    size.width * 0.46f, size.height * 0.50f,
                    size.width * 0.55f, size.height * 0.86f
                )
            },
            color = Color.White.copy(alpha = if (dark) 0.18f else 0.42f),
            style = Stroke(
                width = dimensions.xs.toPx() - dimensions.xxs.toPx() / 4f,
                cap = StrokeCap.Round
            )
        )
        if (showFlag) {
            val poleX = size.width * 0.61f
            val poleTop = size.height * 0.01f
            val poleBottom = size.height * 0.24f
            drawLine(
                color = colors.accent,
                start = Offset(poleX, poleTop),
                end = Offset(poleX, poleBottom),
                strokeWidth = dimensions.xxs.toPx() / 2f,
                cap = StrokeCap.Round
            )
            drawPath(
                path = Path().apply {
                    moveTo(poleX, poleTop)
                    lineTo(poleX + dimensions.icon.toPx(), poleTop + dimensions.xs.toPx() - dimensions.xxs.toPx() / 4f)
                    lineTo(poleX, poleTop + dimensions.md.toPx())
                    close()
                },
                color = colors.accent
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MilestonesScreenPreview() {
    NoContactTheme {
        MilestonesScreen(
            state = MilestonesUiState(
                streakDisplay = "23d 7h 42m 18s",
                currentDays = 23L,
                badges = MilestoneTargets.mapIndexed { i, days ->
                    BadgeState(days, "$days ${if (days == 1) "Day" else "Days"}", i < 3, if (i < 3) "22 Apr 2025, 9:00 AM" else "")
                },
                history = listOf(
                    MilestoneHistoryState(7, "7 Days Milestone", "22 Apr 2025, 8:45 AM", "+4 days"),
                    MilestoneHistoryState(3, "3 Days Milestone", "18 Apr 2025, 9:20 AM", "+2 days"),
                    MilestoneHistoryState(1, "1 Day Milestone", "16 Apr 2025, 9:15 AM", "+1 day")
                ),
                nextMilestoneDays = 30,
                progressToNext = 23f / 30f,
                daysToNext = 7L,
                motivationalMessage = "You're 7 days away from your next milestone!",
                hasProfile = true
            )
        )
    }
}
