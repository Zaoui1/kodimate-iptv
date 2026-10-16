package com.iptvplayer.app.data.model

import android.util.Base64
import com.google.gson.annotations.SerializedName

// ─── Auth ────────────────────────────────────────────────────────────────────

data class AuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo,
    @SerializedName("server_info") val serverInfo: ServerInfo
)

data class UserInfo(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("status") val status: String?,
    @SerializedName("exp_date") val expDate: String?,
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeCons: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("max_connections") val maxConnections: String?,
    @SerializedName("allowed_output_formats") val allowedOutputFormats: List<String>?
)

data class ServerInfo(
    @SerializedName("url") val url: String?,
    @SerializedName("port") val port: String?,
    @SerializedName("https_port") val httpsPort: String?,
    @SerializedName("server_protocol") val serverProtocol: String?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("timestamp_now") val timestampNow: Long?,
    @SerializedName("time_now") val timeNow: String?
)

// ─── Credentials (local) ─────────────────────────────────────────────────────

data class UserCredentials(
    val serverUrl: String,
    val username: String,
    val password: String
)

// ─── Categories ──────────────────────────────────────────────────────────────

data class Category(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int
)

// ─── Live Streams ────────────────────────────────────────────────────────────

data class LiveStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("tv_archive") val tvArchive: Int?,
    @SerializedName("direct_source") val directSource: String?
)

// ─── VOD Streams ─────────────────────────────────────────────────────────────

data class VodStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("rating_5based") val rating5Based: Double?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("direct_source") val directSource: String?
)

// ─── Series ──────────────────────────────────────────────────────────────────

data class Series(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("last_modified") val lastModified: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("rating_5based") val rating5Based: Double?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("category_id") val categoryId: String?
)

// ─── Series Info ─────────────────────────────────────────────────────────────

data class SeriesInfoResponse(
    @SerializedName("info") val info: SeriesDetail?,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>?,
    @SerializedName("seasons") val seasons: List<Season>?
)

data class SeriesDetail(
    @SerializedName("name") val name: String?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("backdrop_path") val backdropPath: Any?
)

data class Episode(
    @SerializedName("id") val id: String,
    @SerializedName("episode_num") val episodeNum: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("season") val season: Int?,
    @SerializedName("direct_source") val directSource: String?
)

data class Season(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("season_number") val seasonNumber: Int?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("cover_big") val coverBig: String?,
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("episode_count") val episodeCount: Int?
)

// ─── EPG ─────────────────────────────────────────────────────────────────────

data class EpgListingsResponse(
    @SerializedName("epg_listings") val epgListings: List<EpgEntry>?
)

data class EpgEntry(
    @SerializedName("id") val id: String?,
    @SerializedName("epg_id") val epgId: String?,
    @SerializedName("title") val titleEncoded: String?,
    @SerializedName("lang") val lang: String?,
    @SerializedName("start") val start: String?,
    @SerializedName("end") val end: String?,
    @SerializedName("description") val descriptionEncoded: String?,
    @SerializedName("channel_id") val channelId: String?,
    @SerializedName("start_timestamp") val startTimestamp: Long?,
    @SerializedName("stop_timestamp") val stopTimestamp: Long?,
    @SerializedName("now_playing") val nowPlaying: Int?
) {
    val title: String
        get() = try {
            String(Base64.decode(titleEncoded ?: "", Base64.DEFAULT)).trim()
        } catch (_: Exception) {
            titleEncoded ?: ""
        }

    val description: String
        get() = try {
            String(Base64.decode(descriptionEncoded ?: "", Base64.DEFAULT)).trim()
        } catch (_: Exception) {
            descriptionEncoded ?: ""
        }

    val isNowPlaying: Boolean get() = nowPlaying == 1

    val durationMinutes: Long
        get() {
            val start = startTimestamp ?: return 0L
            val stop = stopTimestamp ?: return 0L
            return (stop - start) / 60
        }

    fun minutesRemaining(): Long {
        val stop = stopTimestamp ?: return 0L
        val now = System.currentTimeMillis() / 1000
        return maxOf(0L, stop - now) / 60
    }

    fun progressFraction(): Float {
        val start = startTimestamp ?: return 0f
        val stop = stopTimestamp ?: return 0f
        val now = System.currentTimeMillis() / 1000
        if (now < start) return 0f
        if (now > stop) return 1f
        return ((now - start).toFloat() / (stop - start).toFloat()).coerceIn(0f, 1f)
    }
}

// ─── Favorites (local) ───────────────────────────────────────────────────────

enum class FavoriteType { LIVE, VOD, SERIES }

data class FavoriteItem(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val type: FavoriteType,
    val streamId: Int,
    val containerExtension: String? = null
)

// ─── Watch History (local) ───────────────────────────────────────────────────

enum class WatchType { VOD, EPISODE }

data class WatchHistoryItem(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val type: WatchType,
    val streamId: Int,
    val containerExtension: String?,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

// ─── Search results ──────────────────────────────────────────────────────────

data class SearchResults(
    val channels: List<LiveStream>,
    val movies: List<VodStream>,
    val series: List<Series>
)
