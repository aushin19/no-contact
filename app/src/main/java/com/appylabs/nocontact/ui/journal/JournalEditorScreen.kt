package com.appylabs.nocontact.ui.journal

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme

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

    BackHandler(enabled = isDirty && !state.isExistingEntry) {
        showDiscardDialog = true
    }

    JournalEditorScreen(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onMoodChange = viewModel::onMoodChange,
        onSave = viewModel::save,
        onBack = {
            if (isDirty && !state.isExistingEntry) showDiscardDialog = true else onBack()
        },
        onDeleteClick = { showDeleteDialog = true },
        modifier = modifier
    )

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard entry?") },
            text = { Text("You haven't saved this entry. Discard it?") },
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
    val canSave = state.title.isNotBlank() || state.body.isNotBlank()

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
                text = if (state.isExistingEntry) "Edit entry" else "New entry",
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
            Button(
                onClick = onSave,
                enabled = canSave && !state.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                shape = RoundedCornerShape(dimensions.cardRadius),
                contentPadding = PaddingValues(horizontal = dimensions.md, vertical = dimensions.sm)
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = dimensions.screenPadding,
                end = dimensions.screenPadding,
                top = dimensions.sm,
                bottom = dimensions.xxl * 2
            ),
            verticalArrangement = Arrangement.spacedBy(dimensions.lg)
        ) {
            item {
                // Title field
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0f),
                        focusedBorderColor = colors.accent.copy(alpha = 0.4f)
                    ),
                    singleLine = true
                )
            }

            item {
                // Mood selector
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                    Text(
                        text = "HOW ARE YOU FEELING?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(dimensions.sm)) {
                        items(JournalMoods) { mood ->
                            val selected = state.mood == mood
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(dimensions.cardRadius))
                                    .background(
                                        if (selected) colors.accent else MaterialTheme.colorScheme.surfaceContainer
                                    )
                                    .border(
                                        width = dimensions.xxs / 4,
                                        color = if (selected) colors.accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(dimensions.cardRadius)
                                    )
                                    .clickable { onMoodChange(if (selected) "" else mood) }
                                    .padding(horizontal = dimensions.md, vertical = dimensions.sm),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mood,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Body field
                OutlinedTextField(
                    value = state.body,
                    onValueChange = onBodyChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimensions.navHeight * 4),
                    placeholder = {
                        Text(
                            "What's on your mind today?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        focusedBorderColor = colors.accent.copy(alpha = 0.5f)
                    ),
                    minLines = 6
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun JournalEditorPreview() {
    NoContactTheme {
        JournalEditorScreen(
            state = EditorUiState(title = "Tough day", mood = "Hopeful", body = "But I stayed strong."),
            onTitleChange = {}, onBodyChange = {}, onMoodChange = {},
            onSave = {}, onBack = {}, onDeleteClick = {}
        )
    }
}
