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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.ui.components.CategorySidebar
import com.iptvplayer.app.ui.components.ErrorScreen
import com.iptvplayer.app.ui.components.LoadingScreen
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.KodimateCard
import com.iptvplayer.app.ui.theme.KodimateSurface
import com.iptvplayer.app.ui.theme.StarYellow
import com.iptvplayer.app.ui.theme.TextPrimary
import com.iptvplayer.app.ui.theme.TextSecondary
import com.iptvplayer.app.ui.theme.TextTertiary
import com.iptvplayer.app.ui.viewmodel.SeriesListUiState
import com.iptvplayer.app.ui.viewmodel.SeriesViewModel

@Composable
fun SeriesScreen(
    credentials: UserCredentials,
    viewModel: SeriesViewModel,
    onOpenSeriesDetail: (Series) -> Unit
) {
    val uiState by viewModel.listState.collectAsState()

    LaunchedEffect(credentials) {
        if (uiState is SeriesListUiState.Loading) {
            viewModel.loadList(credentials)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground)
    ) {
        when (val state = uiState) {
            is SeriesListUiState.Loading -> LoadingScreen()
            is SeriesListUiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadList(credentials) }
            )
            is SeriesListUiState.Success -> {
                Row(modifier = Modifier.fillMaxSize()) {
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

                    if (state.seriesList.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Text("No series found", color = TextSecondary)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            items(state.seriesList, key = { it.seriesId }) { series ->
                                SeriesCard(
                                    series = series,
                                    isFavorite = viewModel.isFavorite(series.seriesId),
                                    onClick = { onOpenSeriesDetail(series) },
                                    onToggleFavorite = { viewModel.toggleFavorite(series) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesCard(
    series: Series,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(KodimateCard)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (!series.cover.isNullOrBlank()) {
                    AsyncImage(
                        model = series.cover,
                        contentDescription = series.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(KodimateSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(32.dp))
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).size(28.dp).padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) StarYellow else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = series.name,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!series.rating.isNullOrBlank() && series.rating != "0") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Outlined.Star, null, tint = StarYellow, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = series.rating, color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
