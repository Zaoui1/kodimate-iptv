package com.iptvplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.ui.theme.KodimateAccent
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.KodimateSurface
import com.iptvplayer.app.ui.theme.TextSecondary
import com.iptvplayer.app.ui.viewmodel.EpgViewModel
import com.iptvplayer.app.ui.viewmodel.FavoritesViewModel
import com.iptvplayer.app.ui.viewmodel.LiveTvViewModel
import com.iptvplayer.app.ui.viewmodel.MoviesViewModel
import com.iptvplayer.app.ui.viewmodel.SearchViewModel
import com.iptvplayer.app.ui.viewmodel.SeriesViewModel

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    BottomNavItem("Live TV", Icons.Outlined.LiveTv),
    BottomNavItem("Movies", Icons.Outlined.Movie),
    BottomNavItem("Series", Icons.Outlined.Tv),
    BottomNavItem("Favorites", Icons.Outlined.Favorite),
    BottomNavItem("Search", Icons.Outlined.Search)
)

@Composable
fun MainScreen(
    credentials: UserCredentials,
    liveTvVm: LiveTvViewModel,
    moviesVm: MoviesViewModel,
    seriesVm: SeriesViewModel,
    favoritesVm: FavoritesViewModel,
    searchVm: SearchViewModel,
    epgVm: EpgViewModel,
    onPlayStream: (url: String, title: String) -> Unit,
    onOpenSeriesDetail: (Series) -> Unit,
    onOpenEpg: (streamId: Int, channelName: String) -> Unit,
    onOpenSettings: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = KodimateSurface,
                tonalElevation = 0.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KodimateAccent,
                            selectedTextColor = KodimateAccent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = KodimateAccent.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = KodimateBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KodimateBackground)
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> LiveTvScreen(
                    credentials = credentials,
                    viewModel = liveTvVm,
                    epgViewModel = epgVm,
                    onPlayChannel = onPlayStream,
                    onOpenEpg = onOpenEpg,
                    onOpenSettings = onOpenSettings
                )
                1 -> MoviesScreen(
                    credentials = credentials,
                    viewModel = moviesVm,
                    onPlayMovie = onPlayStream
                )
                2 -> SeriesScreen(
                    credentials = credentials,
                    viewModel = seriesVm,
                    onOpenSeriesDetail = onOpenSeriesDetail
                )
                3 -> FavoritesScreen(
                    viewModel = favoritesVm,
                    onPlayItem = onPlayStream,
                    onOpenSeriesDetail = { seriesId ->
                        // Navigate to series using stored data if available
                    }
                )
                4 -> SearchScreen(
                    credentials = credentials,
                    viewModel = searchVm,
                    onPlayStream = onPlayStream,
                    onOpenSeriesDetail = onOpenSeriesDetail
                )
            }
        }
    }
}
