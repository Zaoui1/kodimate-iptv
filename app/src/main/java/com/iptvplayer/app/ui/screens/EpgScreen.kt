package com.iptvplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.app.data.model.EpgEntry
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.ui.components.ErrorScreen
import com.iptvplayer.app.ui.components.LoadingScreen
import com.iptvplayer.app.ui.theme.KodimateAccent
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.KodimateCard
import com.iptvplayer.app.ui.theme.KodimateDivider
import com.iptvplayer.app.ui.theme.KodimateSurface
import com.iptvplayer.app.ui.theme.TextPrimary
import com.iptvplayer.app.ui.theme.TextSecondary
import com.iptvplayer.app.ui.theme.TextTertiary
import com.iptvplayer.app.ui.viewmodel.EpgUiState
import com.iptvplayer.app.ui.viewmodel.EpgViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpgScreen(
    streamId: Int,
    channelName: String,
    credentials: UserCredentials?,
    viewModel: EpgViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedEntry by remember { mutableStateOf<EpgEntry?>(null) }

    LaunchedEffect(streamId, credentials) {
        if (credentials != null) {
            viewModel.loadEpg(credentials, streamId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(text = "TV Guide", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = channelName, color = TextSecondary, fontSize = 12.sp)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = KodimateBackground)
        )

        when (val s = state) {
            is EpgUiState.Loading -> LoadingScreen()
            is EpgUiState.Error -> ErrorScreen(
                message = s.message,
                onRetry = { credentials?.let { viewModel.loadEpg(it, streamId) } }
            )
            is EpgUiState.Idle -> {
                if (credentials == null) {
                    ErrorScreen(message = "Not logged in", onRetry = {})
                } else {
                    LoadingScreen()
                }
            }
            is EpgUiState.Success -> {
                if (s.entries.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No EPG data available for this channel.", color = TextSecondary)
                    }
                } else {
                    EpgTimeline(
                        entries = s.entries,
                        onEntryClick = { selectedEntry = it }
                    )
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            containerColor = KodimateCard,
            title = {
                Text(entry.title, color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val startTime = entry.startTimestamp?.let { timeFormat.format(Date(it * 1000)) }
                    val endTime = entry.stopTimestamp?.let { timeFormat.format(Date(it * 1000)) }
                    if (startTime != null && endTime != null) {
                        Text(
                            text = "$startTime – $endTime • ${entry.durationMinutes} min",
                            color = KodimateAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    val desc = entry.description
                    if (desc.isNotBlank()) {
                        Text(desc, color = TextSecondary, fontSize = 14.sp)
                    } else {
                        Text("No description available.", color = TextTertiary, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEntry = null }) {
                    Text("Close", color = KodimateAccent)
                }
            }
        )
    }
}

@Composable
private fun EpgTimeline(
    entries: List<EpgEntry>,
    onEntryClick: (EpgEntry) -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val nowSec = System.currentTimeMillis() / 1000

    // Group entries by rough time blocks and display as a scrollable timeline
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(entries) { entry ->
            val isNow = (entry.startTimestamp ?: 0) <= nowSec && nowSec <= (entry.stopTimestamp ?: 0)
            EpgEntryRow(
                entry = entry,
                timeFormat = timeFormat,
                isNow = isNow,
                onClick = { onEntryClick(entry) }
            )
        }
    }
}

@Composable
private fun EpgEntryRow(
    entry: EpgEntry,
    timeFormat: SimpleDateFormat,
    isNow: Boolean,
    onClick: () -> Unit
) {
    val startTime = entry.startTimestamp?.let { timeFormat.format(Date(it * 1000)) } ?: "--:--"
    val endTime = entry.stopTimestamp?.let { timeFormat.format(Date(it * 1000)) } ?: "--:--"
    val bg = if (isNow) KodimateAccent.copy(alpha = 0.12f) else KodimateBackground
    val borderColor = if (isNow) KodimateAccent else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bg)
            .then(
                if (isNow) Modifier.border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(0.dp)
                ) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time column
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                color = if (isNow) KodimateAccent else TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = endTime,
                color = TextTertiary,
                fontSize = 11.sp
            )
            Text(
                text = "${entry.durationMinutes}m",
                color = TextTertiary,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isNow) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(KodimateAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = entry.title.ifBlank { "Unknown Programme" },
                    color = if (isNow) TextPrimary else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (isNow) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isNow) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { entry.progressFraction() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = KodimateAccent,
                    trackColor = KodimateDivider
                )
                val remaining = entry.minutesRemaining()
                if (remaining > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${remaining}m remaining",
                        color = KodimateAccent,
                        fontSize = 10.sp
                    )
                }
            } else {
                val desc = entry.description
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        color = TextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(KodimateDivider)
    )
}
