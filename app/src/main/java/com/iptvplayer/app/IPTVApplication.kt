package com.iptvplayer.app

import android.app.Application
import com.iptvplayer.app.data.local.CredentialsDataStore
import com.iptvplayer.app.data.local.FavoritesDataStore
import com.iptvplayer.app.data.local.WatchHistoryDataStore
import com.iptvplayer.app.data.repository.XtreamRepository

class IPTVApplication : Application() {

    val repository: XtreamRepository by lazy {
        XtreamRepository(
            credentialsDataStore = CredentialsDataStore(this),
            favoritesDataStore = FavoritesDataStore(this),
            watchHistoryDataStore = WatchHistoryDataStore(this)
        )
    }
}
