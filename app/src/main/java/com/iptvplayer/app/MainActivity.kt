package com.iptvplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.iptvplayer.app.ui.navigation.AppNavGraph
import com.iptvplayer.app.ui.theme.KodimateBackground
import com.iptvplayer.app.ui.theme.KodimateTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as IPTVApplication).repository

        setContent {
            KodimateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KodimateBackground
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }
}
