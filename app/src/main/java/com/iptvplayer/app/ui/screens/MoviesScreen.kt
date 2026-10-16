package com.iptvplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.model.VodStream
import com.iptvplayer.app.data.model.WatchHistoryItem
import com.iptvplayer.app.ui.components.CategorySidebar
import com.iptvplayer.app.ui.components.ErrorScreen
import com.iptvplayer.app.ui.components.LoadingScreen
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
import com.iptvplayer.app.ui.viewmodel.MoviesUiState
import com.iptvplayer.app.ui.viewmodel.MoviesViewModel

@Composable
fun MoviesScreen(
    credentials: UserCredentials,
    viewModel: MoviesViewModel,
    onPlayMovie: (url: String, title: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(credentials) {
        if (uiState is MoviesUiState.Loading) {
            viewModel.load(credentials)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground)
    ) {
        when (val state = uiState) {
            is MoviesUiState.Loading -> LoadingScreen()
            is MoviesUiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { viewModel.load(credentials) }
            )
            is MoviesUiState.Success -> {
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

                    // Main content
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        // Continue Watching row (collected separately via FavoritesViewModel)
                        // Movies grid
                        if (state.movies.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No movies found", color = TextSecondary)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.movies, key = { it.streamId }) { movie ->
                                    MovieCard(
                                        movie = movie,
                                        isFavorite = viewModel.isFavorite(movie.streamId),
                                        onPlay = {
                                            onPlayMovie(viewModel.buildStreamUrl(movie), movie.name)
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(movie) }
                                    )
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
fun MovieCard(
    movie: VodStream,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    watchItem: WatchHistoryItem? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(KodimateCard)
            .clickable(onClick = onPlay)
    ) {
        Column {
            // Poster
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (!movie.streamIcon.isNullOrBlank()) {
                    AsyncImage(
                        model = movie.streamIcon,
                        contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(KodimateSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(32.dp))
                    }
                }

                // Favorite button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) StarYellow else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Continue watching progress
                if (watchItem != null && watchItem.progressFraction > 0.01f) {
                    LinearProgressIndicator(
                        progress = { watchItem.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = KodimateAccent,
                        trackColor = KodimateDivider
                    )
                }
            }

            // Title + rating
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = movie.name,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!movie.rating.isNullOrBlank() && movie.rating != "0") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Outlined.Star, null, tint = StarYellow, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = movie.rating, color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingRow(
    items: List<WatchHistoryItem>,
    onPlay: (url: String, title: String) -> Unit,
    buildUrl: (WatchHistoryItem) -> String,
    onDismiss: (String) -> Unit
) {
    if (items.isEmpty()) return

    Column {
        SectionHeader(title = "Continue Watching")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ContinueWatchingCard(
                    item = item,
                    onPlay = { onPlay(buildUrl(item), item.title) },
                    onDismiss = { onDismiss(item.id) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ContinueWatchingCard(
    item: WatchHistoryItem,
    onPlay: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(KodimateCard)
            .clickable(onClick = onPlay)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
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
                        Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(28.dp))
                    }
                }
                if (item.progressFraction > 0.01f) {
                    LinearProgressIndicator(
                        progress = { item.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = KodimateAccent,
                        trackColor = KodimateDivider
                    )
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
