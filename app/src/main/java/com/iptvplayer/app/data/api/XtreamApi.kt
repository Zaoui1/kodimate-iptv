package com.iptvplayer.app.data.api

import com.iptvplayer.app.data.model.AuthResponse
import com.iptvplayer.app.data.model.Category
import com.iptvplayer.app.data.model.EpgListingsResponse
import com.iptvplayer.app.data.model.LiveStream
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.SeriesInfoResponse
import com.iptvplayer.app.data.model.VodStream
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApi {

    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): AuthResponse

    // ─── Live ────────────────────────────────────────────────────────────────

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String
    ): List<Category>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String,
        @Query("category_id") categoryId: String?
    ): List<LiveStream>

    // ─── VOD ─────────────────────────────────────────────────────────────────

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String
    ): List<Category>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String,
        @Query("category_id") categoryId: String?
    ): List<VodStream>

    // ─── Series ──────────────────────────────────────────────────────────────

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String
    ): List<Category>

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String,
        @Query("category_id") categoryId: String?
    ): List<Series>

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String,
        @Query("series_id") seriesId: Int
    ): SeriesInfoResponse

    // ─── EPG ─────────────────────────────────────────────────────────────────

    @GET("player_api.php")
    suspend fun getEpg(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String,
        @Query("stream_id") streamId: Int
    ): EpgListingsResponse
}
