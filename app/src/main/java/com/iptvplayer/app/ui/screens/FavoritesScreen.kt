package com.iptvplayer.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.data.model.FavoriteItem
import com.iptvplayer.app.data.model.WatchHistoryItem
import com.iptvplayer.app.ui.components.SectionHeader
import com.iptvplayer.app.ui.theme.KodimateAccent
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.KodimateCard
import com.iptvplayer.app.ui.theme.KodimateDivider
import com.iptvplayer.app.ui.theme.KodimateSurface
import com.iptvplayer.app.ui.theme.StarYellow
import com.iptvplayer.app.ui.theme.TextPrimary
import com.iptvplayer.app.ui.theme.TextSecondary
import com.iptvplayer.app.ui.theme.TextTertiary
import com.iptvplayer.app.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onPlayItem: (url: String, title: String) -> Unit,
    onOpenSeriesDetail: (seriesId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    val hasAnyFavorites = uiState.liveItems.isNotEmpty() ||
            uiState.vodItems.isNotEmpty() ||
            uiState.seriesItems.isNotEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground)
    ) {
        item {
            Text(
                text = "Favorites",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Continue Watching section
        if (watchHistory.isNotEmpty()) {
            item {
                SectionHeader(title = "Continue Watching")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchHistory.take(10), key = { "hist_${it.id}" }) { item ->
                        WatchHistoryCard(
                            item = item,
                            onPlay = {
                                onPlayItem(viewModel.buildWatchHistoryUrl(item), item.title)
                            },
                            onRemove = { viewModel.removeWatchHistoryItem(item.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (!hasAnyFavorites && watchHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Favorite,
                            null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No favorites yet",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Text(
                            "Tap the heart icon on any channel,\nmovie, or series to save it here.",
                            color = TextTertiary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Live TV favorites
        if (uiState.liveItems.isNotEmpty()) {
            item { FavoritesSectionHeader(title = "Live TV", icon = Icons.Outlined.LiveTv, count = uiState.liveItems.size) }
            items(uiState.liveItems, key = { "fav_${it.id}" }) { item ->
                FavoriteListItem(
                    item = item,
                    onPlay = { onPlayItem(viewModel.buildStreamUrl(item), item.name) },
                    onRemove = { viewModel.removeFavorite(item.id) }
                )
            }
        }

        // Movies favorites
        if (uiState.vodItems.isNotEmpty()) {
            item { FavoritesSectionHeader(title = "Movies", icon = Icons.Outlined.Movie, count = uiState.vodItems.size) }
            items(uiState.vodItems, key = { "fav_${it.id}" }) { item ->
                FavoriteListItem(
                    item = item,
                    onPlay = { onPlayItem(viewModel.buildStreamUrl(item), item.name) },
                    onRemove = { viewModel.removeFavorite(item.id) }
                )
            }
        }

        // Series favorites
        if (uiState.seriesItems.isNotEmpty()) {
            item { FavoritesSectionHeader(title = "Series", icon = Icons.Outlined.Tv, count = uiState.seriesItems.size) }
            items(uiState.seriesItems, key = { "fav_${it.id}" }) { item ->
                FavoriteListItem(
                    item = item,
                    onPlay = { onOpenSeriesDetail(item.streamId) },
                    onRemove = { viewModel.removeFavorite(item.id) },
                    playLabel = "Open"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun FavoritesSectionHeader(title: String, icon: ImageVector, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = KodimateAccent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text("$count", color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun FavoriteListItem(
    item: FavoriteItem,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    playLabel: String = "Play"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(KodimateCard),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(onClick = onRemove) {
            Icon(Icons.Outlined.Delete, "Remove", tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KodimateDivider))
}

@Composable
private fun WatchHistoryCard(
    item: WatchHistoryItem,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(KodimateCard)
            .clickable(onClick = onPlay)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(85.dp)) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(KodimateSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(24.dp))
                    }
                }
                if (item.progressFraction > 0.01f) {
                    LinearProgressIndicator(
                        progress = { item.progressFraction },
                        modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomCenter),
                        color = KodimateAccent,
                        trackColor = KodimateDivider
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                ) {
                    Icon(Icons.Outlined.Close, "Remove", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}
