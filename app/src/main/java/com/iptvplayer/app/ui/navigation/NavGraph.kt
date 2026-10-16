package com.iptvplayer.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.repository.XtreamRepository
import com.iptvplayer.app.ui.screens.EpgScreen
import com.iptvplayer.app.ui.screens.LoginScreen
import com.iptvplayer.app.ui.screens.MainScreen
import com.iptvplayer.app.ui.screens.PlayerScreen
import com.iptvplayer.app.ui.screens.SeriesDetailScreen
import com.iptvplayer.app.ui.screens.SettingsScreen
import com.iptvplayer.app.ui.screens.SplashScreen
import com.iptvplayer.app.ui.viewmodel.EpgViewModel
import com.iptvplayer.app.ui.viewmodel.FavoritesViewModel
import com.iptvplayer.app.ui.viewmodel.LiveTvViewModel
import com.iptvplayer.app.ui.viewmodel.LoginViewModel
import com.iptvplayer.app.ui.viewmodel.MoviesViewModel
import com.iptvplayer.app.ui.viewmodel.SearchViewModel
import com.iptvplayer.app.ui.viewmodel.SeriesViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val PLAYER = "player"
    const val SERIES_DETAIL = "series_detail/{seriesId}"
    const val EPG = "epg/{streamId}/{channelName}"

    fun player(url: String, title: String): String =
        "player?url=${Uri.encode(url)}&title=${Uri.encode(title)}"

    fun seriesDetail(seriesId: Int): String = "series_detail/$seriesId"
    fun epg(streamId: Int, channelName: String): String =
        "epg/$streamId/${Uri.encode(channelName)}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: XtreamRepository
) {
    // Shared ViewModels scoped to nav graph lifetime
    val loginVm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(repository))
    val liveTvVm: LiveTvViewModel = viewModel(factory = LiveTvViewModel.Factory(repository))
    val moviesVm: MoviesViewModel = viewModel(factory = MoviesViewModel.Factory(repository))
    val seriesVm: SeriesViewModel = viewModel(factory = SeriesViewModel.Factory(repository))
    val favoritesVm: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory(repository))
    val searchVm: SearchViewModel = viewModel(factory = SearchViewModel.Factory(repository))
    val epgVm: EpgViewModel = viewModel(factory = EpgViewModel.Factory(repository))

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                repository = repository,
                onLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNotLoggedIn = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = loginVm,
                onLoginSuccess = { creds ->
                    liveTvVm.load(creds)
                    moviesVm.load(creds)
                    seriesVm.loadList(creds)
                    favoritesVm.setCredentials(creds)
                    searchVm.setCredentials(creds)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            val savedCreds by repository.credentialsFlow.collectAsState(initial = null)
            val creds = savedCreds
            if (creds != null) {
                MainScreen(
                    credentials = creds,
                    liveTvVm = liveTvVm,
                    moviesVm = moviesVm,
                    seriesVm = seriesVm,
                    favoritesVm = favoritesVm,
                    searchVm = searchVm,
                    epgVm = epgVm,
                    onPlayStream = { url, title ->
                        navController.navigate(Routes.player(url, title))
                    },
                    onOpenSeriesDetail = { series ->
                        seriesVm.loadSeriesDetail(series)
                        navController.navigate(Routes.seriesDetail(series.seriesId))
                    },
                    onOpenEpg = { streamId, channelName ->
                        navController.navigate(Routes.epg(streamId, channelName))
                    },
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
            }
        }

        composable(
            route = "${Routes.PLAYER}?url={url}&title={title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType; defaultValue = "" },
                navArgument("title") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            val url = back.arguments?.getString("url") ?: ""
            val title = back.arguments?.getString("title") ?: ""
            val savedCreds by repository.credentialsFlow.collectAsState(initial = null)
            PlayerScreen(
                streamUrl = url,
                title = title,
                repository = repository,
                credentials = savedCreds,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SERIES_DETAIL,
            arguments = listOf(navArgument("seriesId") { type = NavType.IntType })
        ) {
            SeriesDetailScreen(
                viewModel = seriesVm,
                onPlayEpisode = { url, title ->
                    navController.navigate(Routes.player(url, title))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EPG,
            arguments = listOf(
                navArgument("streamId") { type = NavType.IntType },
                navArgument("channelName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            val streamId = back.arguments?.getInt("streamId") ?: 0
            val channelName = back.arguments?.getString("channelName") ?: ""
            val savedCreds by repository.credentialsFlow.collectAsState(initial = null)
            EpgScreen(
                streamId = streamId,
                channelName = channelName,
                credentials = savedCreds,
                viewModel = epgVm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                repository = repository,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
