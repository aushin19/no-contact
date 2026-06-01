package com.appylabs.nocontact.ui.journal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiObjects
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class SimpleMood(val label: String, val icon: ImageVector)

internal val SimpleMoods = listOf(
    SimpleMood("Very Bad", Icons.Rounded.SentimentVeryDissatisfied),
    SimpleMood("Bad", Icons.Rounded.SentimentDissatisfied),
    SimpleMood("Neutral", Icons.Rounded.SentimentNeutral),
    SimpleMood("Good", Icons.Rounded.SentimentSatisfied),
    SimpleMood("Very Good", Icons.Rounded.SentimentSatisfiedAlt)
)

@Composable
fun JournalEditorRoute(
    entryId: Int?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: JournalEditorViewModel = viewModel(
        key = "editor_${entryId}",
        factory = JournalEditorViewModel.Factory(application.repository, entryId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is EditorEvent.Saved, EditorEvent.Deleted -> onBack()
            }
        }
    }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isDirty = state.title.isNotBlank() || state.body.isNotBlank()

    BackHandler(enabled = isDirty) {
        showDiscardDialog = true
    }

    JournalEditorScreen(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onMoodChange = viewModel::onMoodChange,
        onSave = viewModel::save,
        onBack = {
            if (isDirty) showDiscardDialog = true else onBack()
        },
        onDeleteClick = { showDeleteDialog = true },
        modifier = modifier
    )

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(if (state.isExistingEntry) "Discard changes?" else "Discard entry?") },
            text = { Text(if (state.isExistingEntry) "Your unsaved changes will be lost." else "You haven't saved this entry. Discard it?") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep writing") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete entry?") },
            text = { Text("This will permanently delete this journal entry.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.delete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun JournalEditorScreen(
    state: EditorUiState,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onMoodChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val totalChars = state.title.trim().length + state.body.trim().length
    val canSave = totalChars >= 10

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.sm, vertical = dimensions.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = if (state.isExistingEntry) "Edit Journal Entry" else "New Journal Entry",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = dimensions.sm)
            )
            if (state.isExistingEntry) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            TextButton(
                onClick = onSave,
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

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(
                start = dimensions.screenPadding,
                end = dimensions.screenPadding,
                top = dimensions.sm,
                bottom = dimensions.navHeight + dimensions.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(dimensions.lg)
        ) {
            // Title field
            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Entry title",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
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
            }

            // Mood selector
            item {
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
                                onClick = { onMoodChange(if (selected) "" else mood.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Body field
            item {
                Column {
                    OutlinedTextField(
                        value = state.body,
                        onValueChange = { if (it.length <= 1000) onBodyChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = dimensions.navHeight * 4),
                        placeholder = {
                            Text(
                                "Write your thoughts\u2026",
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
                        minLines = 6
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
            }

            // Writing Tip card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensions.cardRadius),
                    color = colors.softIconContainer,
                    border = BorderStroke(dimensions.xxs / 4, colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(dimensions.md),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiObjects,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(dimensions.icon)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(dimensions.xxs)
                        ) {
                            Text(
                                text = "WRITING TIP",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.accent
                            )
                            Text(
                                text = "There\u2019s no right or wrong way to journal. Just write what\u2019s on your mind.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Metadata rows
            item {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    MetadataRow(
                        label = "Date",
                        value = formatDisplayDate(state.createdAtMillis),
                        trailingIcon = Icons.Rounded.Edit
                    )
                    MetadataRow(
                        label = "Tags",
                        value = "Add tags",
                        trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                    )
                    MetadataRow(
                        label = "Privacy",
                        value = "Private",
                        trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            // Bottom Save Entry + Discard buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                    Button(
                        onClick = onSave,
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
                    if (!state.isExistingEntry) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Discard",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MoodChip(
    mood: SimpleMood,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cardRadius))
            .background(if (selected) colors.accentSoft else MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = dimensions.xxs / 4,
                color = if (selected) colors.accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(dimensions.cardRadius)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = dimensions.xxs, vertical = dimensions.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.xxs)
    ) {
        Icon(
            imageVector = mood.icon,
            contentDescription = mood.label,
            tint = if (selected) colors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensions.xl)
        )
        Text(
            text = mood.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MetadataRow(label: String, value: String, trailingIcon: ImageVector) {
    val dimensions = LocalNoContactDimensions.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.md, vertical = dimensions.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensions.icon)
        )
    }
}

private fun formatDisplayDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun JournalEditorPreview() {
    NoContactTheme {
        JournalEditorScreen(
            state = EditorUiState(title = "Tough day", mood = "Good", body = "But I stayed strong."),
            onTitleChange = {}, onBodyChange = {}, onMoodChange = {},
            onSave = {}, onBack = {}, onDeleteClick = {}
        )
    }
}
