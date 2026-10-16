package com.iptvplayer.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.model.WatchType
import com.iptvplayer.app.data.repository.XtreamRepository
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PlayerScreen(
    streamUrl: String,
    title: String,
    repository: XtreamRepository,
    credentials: UserCredentials?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Lock to landscape
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            if (streamUrl.isNotBlank()) {
                setMediaItem(MediaItem.fromUri(streamUrl))
                prepare()
                playWhenReady = true
            }
        }
    }

    // Periodically save watch position (for VOD/episodes)
    val isVod = streamUrl.contains("/movie/") || streamUrl.contains("/series/")
    var savedPositionMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isVod) {
        if (isVod && credentials != null) {
            while (isActive) {
                delay(10_000L) // save every 10s
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration.takeIf { it > 0 } ?: 0L
                if (pos > 0 && dur > 0) {
                    savedPositionMs = pos
                    val histId = "vod_${streamUrl.hashCode()}"
                    val type = if (streamUrl.contains("/movie/")) WatchType.VOD else WatchType.EPISODE
                    repository.saveWatchPosition(
                        id = histId,
                        title = title,
                        imageUrl = null,
                        type = type,
                        streamId = 0,
                        containerExtension = null,
                        positionMs = pos,
                        durationMs = dur
                    )
                }
            }
        }
    }

    DisposableEffect(streamUrl) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Back button overlay
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
    }
}
