package com.iptvplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.data.model.EpgEntry
import com.iptvplayer.app.data.model.LiveStream
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.ui.components.CategorySidebar
import com.iptvplayer.app.ui.components.ErrorScreen
import com.iptvplayer.app.ui.components.LoadingScreen
import com.iptvplayer.app.ui.theme.KodimateAccent
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.KodimateCard
import com.iptvplayer.app.ui.theme.KodimateDivider
import com.iptvplayer.app.ui.theme.KodimateSurface
import com.iptvplayer.app.ui.theme.StarYellow
import com.iptvplayer.app.ui.theme.TextPrimary
import com.iptvplayer.app.ui.theme.TextSecondary
import com.iptvplayer.app.ui.theme.TextTertiary
import com.iptvplayer.app.ui.viewmodel.EpgViewModel
import com.iptvplayer.app.ui.viewmodel.LiveTvUiState
import com.iptvplayer.app.ui.viewmodel.LiveTvViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTvScreen(
    credentials: UserCredentials,
    viewModel: LiveTvViewModel,
    epgViewModel: EpgViewModel,
    onPlayChannel: (url: String, title: String) -> Unit,
    onOpenEpg: (streamId: Int, channelName: String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(credentials) {
        if (uiState is LiveTvUiState.Loading || (uiState as? LiveTvUiState.Success)?.streams?.isEmpty() == true) {
            viewModel.load(credentials)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Live TV",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Outlined.Settings, "Settings", tint = TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = KodimateBackground)
        )

        when (val state = uiState) {
            is LiveTvUiState.Loading -> LoadingScreen()
            is LiveTvUiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { viewModel.load(credentials) }
            )
            is LiveTvUiState.Success -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Category sidebar
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .fillMaxHeight()
                            .background(KodimateSurface)
                    ) {
                        CategorySidebar(
                            categories = state.categories,
                            selectedCategoryId = state.selectedCategoryId,
                            onSelectCategory = viewModel::selectCategory
                        )
                    }

                    // Channel list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        items(state.streams, key = { it.streamId }) { stream ->
                            val currentProgram = epgViewModel.currentProgram(stream.streamId)
                            ChannelListItem(
                                stream = stream,
                                currentProgram = currentProgram,
                                isFavorite = viewModel.isFavorite(stream.streamId),
                                onPlay = {
                                    onPlayChannel(viewModel.buildStreamUrl(stream.streamId), stream.name)
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(stream) },
                                onOpenEpg = {
                                    // Lazy load EPG for this channel
                                    epgViewModel.loadEpg(credentials, stream.streamId)
                                    onOpenEpg(stream.streamId, stream.name)
                                }
                            )
                        }
                        if (state.streams.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No channels found", color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelListItem(
    stream: LiveStream,
    currentProgram: EpgEntry?,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenEpg: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel logo
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(KodimateCard),
            contentAlignment = Alignment.Center
        ) {
            if (!stream.streamIcon.isNullOrBlank()) {
                AsyncImage(
                    model = stream.streamIcon,
                    contentDescription = stream.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = stream.name.take(2).uppercase(),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stream.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (currentProgram != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentProgram.title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val remaining = currentProgram.minutesRemaining()
                if (remaining > 0) {
                    Text(
                        text = "${remaining}m remaining",
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { currentProgram.progressFraction() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp)),
                        color = KodimateAccent,
                        trackColor = KodimateDivider
                    )
                }
            }
        }

        // EPG button
        IconButton(onClick = onOpenEpg, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.Info, "EPG", tint = TextTertiary, modifier = Modifier.size(18.dp))
        }

        // Favorite button
        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) StarYellow else TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(KodimateDivider)
    )
}
