package com.iptvplayer.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.app.data.repository.XtreamRepository
import com.iptvplayer.app.ui.theme.KodimateAccent
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.TextPrimary
import com.iptvplayer.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    repository: XtreamRepository,
    onLoggedIn: () -> Unit,
    onNotLoggedIn: () -> Unit
) {
    val credentials by repository.credentialsFlow.collectAsState(initial = null)
    var ready by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1800)
        ready = true
    }

    LaunchedEffect(ready, credentials) {
        if (ready) {
            if (credentials != null) onLoggedIn() else onNotLoggedIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KodimateBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            // "K" icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(KodimateAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "K",
                    color = TextPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Kodimate",
                color = TextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Premium IPTV Experience",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
