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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.data.model.Episode
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
import com.iptvplayer.app.ui.viewmodel.SeriesDetailUiState
import com.iptvplayer.app.ui.viewmodel.SeriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    viewModel: SeriesViewModel,
    onPlayEpisode: (url: String, title: String) -> Unit,
    onBack: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground)
    ) {
        when (val state = detailState) {
            is SeriesDetailUiState.Idle,
            is SeriesDetailUiState.Loading -> {
                TopAppBar(
                    title = { Text("Series", color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KodimateBackground)
                )
                LoadingScreen()
            }
            is SeriesDetailUiState.Error -> {
                TopAppBar(
                    title = { Text("Series", color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KodimateBackground)
                )
                ErrorScreen(message = state.message, onRetry = {})
            }
            is SeriesDetailUiState.Success -> {
                val series = state.series
                val isFav = favorites.any { it.id == "series_${series.seriesId}" }

                // Episodes for selected season
                val seasonKey = state.selectedSeason.toString()
                val episodes = state.info.episodes?.get(seasonKey) ?: emptyList()
                val seasons = state.info.episodes?.keys?.mapNotNull { it.toIntOrNull() }?.sorted() ?: emptyList()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Header with cover + info
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                            val coverUrl = state.info.info?.cover ?: series.cover
                            if (!coverUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = series.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    Modifier.fillMaxSize().background(KodimateSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                                }
                            }

                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                KodimateBackground
                                            )
                                        )
                                    )
                            )

                            // Back + Favorite buttons
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                                }
                                IconButton(onClick = { viewModel.toggleFavorite(series) }) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isFav) StarYellow else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Series info
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = series.name,
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                if (!series.rating.isNullOrBlank() && series.rating != "0") {
                                    Icon(Icons.Outlined.Star, null, tint = StarYellow, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(series.rating, color = TextSecondary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                if (!series.releaseDate.isNullOrBlank()) {
                                    Text(series.releaseDate, color = TextSecondary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                if (!series.genre.isNullOrBlank()) {
                                    Text(series.genre, color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                            val plot = state.info.info?.plot ?: series.plot
                            if (!plot.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(plot, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
                            }
                        }
                    }

                    // Season selector
                    if (seasons.isNotEmpty()) {
                        item {
                            Text(
                                "Seasons",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(seasons) { season ->
                                    FilterChip(
                                        selected = state.selectedSeason == season,
                                        onClick = { viewModel.selectSeason(season) },
                                        label = {
                                            Text("Season $season", fontSize = 13.sp)
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = KodimateAccent,
                                            selectedLabelColor = TextPrimary,
                                            containerColor = KodimateCard,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Episodes
                    item {
                        Text(
                            "${episodes.size} Episode${if (episodes.size != 1) "s" else ""}",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    items(episodes) { episode ->
                        EpisodeItem(
                            episode = episode,
                            seriesName = series.name,
                            onPlay = {
                                val url = viewModel.buildEpisodeUrl(episode)
                                val title = "${series.name} · ${episode.title ?: "Episode ${episode.episodeNum}"}"
                                onPlayEpisode(url, title)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EpisodeItem(
    episode: Episode,
    seriesName: String,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(KodimateCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${episode.episodeNum ?: "-"}",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = episode.title ?: "Episode ${episode.episodeNum}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            Icons.Outlined.PlayArrow,
            contentDescription = "Play",
            tint = KodimateAccent,
            modifier = Modifier.size(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(KodimateDivider)
    )
}
