package com.appylabs.nocontact.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appylabs.nocontact.NoContactApplication
import com.appylabs.nocontact.data.JournalEntryEntity
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions
import com.appylabs.nocontact.ui.theme.NoContactTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalDetailRoute(
    entryId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val application = LocalContext.current.applicationContext as NoContactApplication
    val viewModel: JournalDetailViewModel = viewModel(
        key = "detail_$entryId",
        factory = JournalDetailViewModel.Factory(application.repository, entryId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DetailEvent.Deleted -> onBack()
            }
        }
    }

    // If entry disappears (deleted from elsewhere), go back
    LaunchedEffect(state.isLoading, state.entry) {
        if (!state.isLoading && state.entry == null) onBack()
    }

    JournalDetailScreen(
        state = state,
        onBack = onBack,
        onEdit = { state.entry?.let { onEdit(it.id) } },
        onDeleteClick = { showDeleteDialog = true }
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalDetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.entry?.title?.ifBlank { "Entry" } ?: "Entry",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete entry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit entry",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val entry = state.entry
        if (entry != null) {
            EntryContent(
                entry = entry,
                contentPadding = innerPadding
            )
        }
        // While loading or null — just empty (back nav fires via LaunchedEffect)
    }
}

@Composable
private fun EntryContent(
    entry: JournalEntryEntity,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val moodColor = moodToColor(entry.mood, colors)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(
                horizontal = dimensions.screenPadding,
                vertical = dimensions.md
            )
    ) {
        // Title
        if (entry.title.isNotBlank()) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(dimensions.sm))
        }

        // Date + mood row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDetailDate(entry.createdAtMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (entry.mood.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimensions.pillRadius))
                        .background(moodColor.copy(alpha = 0.14f))
                        .padding(horizontal = dimensions.md, vertical = dimensions.xxs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.mood,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = moodColor
                    )
                }
            }
        }

        Spacer(Modifier.height(dimensions.lg))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(dimensions.lg))

        // Body
        if (entry.body.isNotBlank()) {
            Text(
                text = entry.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = "No body text.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(dimensions.xxl * 2))
    }
}

private fun formatDetailDate(millis: Long): String =
    SimpleDateFormat("EEEE, d MMMM yyyy  ·  h:mm a", Locale.getDefault()).format(Date(millis))

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun JournalDetailPreview() {
    NoContactTheme {
        JournalDetailScreen(
            state = DetailUiState(
                entry = JournalEntryEntity(
                    id = 1,
                    title = "Tough day but I stayed strong",
                    body = "I almost reached out but I didn't. I went for a walk instead and it helped a lot. Small wins matter.",
                    mood = "Hopeful",
                    createdAtMillis = System.currentTimeMillis()
                ),
                isLoading = false
            ),
            onBack = {},
            onEdit = {},
            onDeleteClick = {}
        )
    }
}
