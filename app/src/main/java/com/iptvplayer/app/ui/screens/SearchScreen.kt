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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.data.model.LiveStream
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.model.VodStream
import com.iptvplayer.app.ui.components.SectionHeader
import com.iptvplayer.app.ui.theme.KodimateAccent
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.KodimateCard
import com.iptvplayer.app.ui.theme.KodimateDivider
import com.iptvplayer.app.ui.theme.KodimateSurface
import com.iptvplayer.app.ui.theme.TextPrimary
import com.iptvplayer.app.ui.theme.TextSecondary
import com.iptvplayer.app.ui.theme.TextTertiary
import com.iptvplayer.app.ui.viewmodel.SearchUiState
import com.iptvplayer.app.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    credentials: UserCredentials,
    viewModel: SearchViewModel,
    onPlayStream: (url: String, title: String) -> Unit,
    onOpenSeriesDetail: (Series) -> Unit
) {
    val query by viewModel.query.collectAsState()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(credentials) {
        viewModel.setCredentials(credentials)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground)
    ) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KodimateBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search channels, movies, series…", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextSecondary) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(Icons.Outlined.Clear, "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = KodimateAccent,
                    unfocusedBorderColor = KodimateDivider,
                    containerColor = KodimateCard,
                    cursorColor = KodimateAccent
                )
            )
        }

        when (val s = state) {
            is SearchUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Search,
                            null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Start typing to search", color = TextSecondary, fontSize = 15.sp)
                        Text(
                            "Search across live channels, movies and series",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp)
                        )
                    }
                }
            }
            is SearchUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KodimateAccent)
                }
            }
            is SearchUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = TextSecondary)
                }
            }
            is SearchUiState.Success -> {
                val results = s.results
                val total = results.channels.size + results.movies.size + results.series.size
                if (total == 0) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No results for "$query"", color = TextSecondary, fontSize = 15.sp)
                            Text("Try a different search term", color = TextTertiary, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (results.channels.isNotEmpty()) {
                            item { SearchSectionHeader("Live TV", Icons.Outlined.LiveTv, results.channels.size) }
                            items(results.channels.take(10)) { ch ->
                                SearchChannelItem(
                                    channel = ch,
                                    onClick = {
                                        onPlayStream(viewModel.buildLiveUrl(ch.streamId), ch.name)
                                    }
                                )
                            }
                        }

                        if (results.movies.isNotEmpty()) {
                            item { SearchSectionHeader("Movies", Icons.Outlined.Movie, results.movies.size) }
                            items(results.movies.take(10)) { movie ->
                                SearchMovieItem(
                                    movie = movie,
                                    onClick = {
                                        onPlayStream(viewModel.buildVodUrl(movie), movie.name)
                                    }
                                )
                            }
                        }

                        if (results.series.isNotEmpty()) {
                            item { SearchSectionHeader("Series", Icons.Outlined.Tv, results.series.size) }
                            items(results.series.take(10)) { series ->
                                SearchSeriesItem(
                                    series = series,
                                    onClick = { onOpenSeriesDetail(series) }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String, icon: ImageVector, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = KodimateAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text("$count", color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun SearchChannelItem(channel: LiveStream, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(KodimateCard),
            contentAlignment = Alignment.Center
        ) {
            if (!channel.streamIcon.isNullOrBlank()) {
                AsyncImage(channel.streamIcon, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else {
                Text(channel.name.take(2).uppercase(), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(channel.name, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KodimateDivider))
}

@Composable
private fun SearchMovieItem(movie: VodStream, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(KodimateCard),
            contentAlignment = Alignment.Center
        ) {
            if (!movie.streamIcon.isNullOrBlank()) {
                AsyncImage(movie.streamIcon, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Outlined.Movie, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(movie.name, color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!movie.rating.isNullOrBlank() && movie.rating != "0") {
                Text("★ ${movie.rating}", color = TextSecondary, fontSize = 11.sp)
            }
        }
        Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KodimateDivider))
}

@Composable
private fun SearchSeriesItem(series: Series, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(KodimateCard),
            contentAlignment = Alignment.Center
        ) {
            if (!series.cover.isNullOrBlank()) {
                AsyncImage(series.cover, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Outlined.Tv, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(series.name, color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!series.genre.isNullOrBlank()) {
                Text(series.genre, color = TextSecondary, fontSize = 11.sp)
            }
        }
        Icon(Icons.Outlined.PlayArrow, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KodimateDivider))
}
