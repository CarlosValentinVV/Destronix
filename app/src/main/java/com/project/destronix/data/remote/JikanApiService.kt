package com.project.destronix.data.remote

import com.project.destronix.data.remote.dto.AnimeDto
import com.project.destronix.data.remote.dto.CharacterWrapperDto
import com.project.destronix.data.remote.dto.GenreDto
import com.project.destronix.data.remote.dto.JikanDataResponse
import com.project.destronix.data.remote.dto.JikanListResponse
import com.project.destronix.data.remote.dto.JikanVideosDto
import com.project.destronix.data.remote.dto.RecommendationDto
import com.project.destronix.data.remote.dto.RelationDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** API pública de Jikan (v4), sin necesidad de credenciales. */
interface JikanApiService {

    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("filter") filter: String? = null,
        @Query("limit") limit: Int = 24,
        @Query("page") page: Int = 1,
    ): JikanListResponse<AnimeDto>

    @GET("seasons/now")
    suspend fun getCurrentSeason(
        @Query("limit") limit: Int = 24,
        @Query("page") page: Int = 1,
    ): JikanListResponse<AnimeDto>

    @GET("genres/anime")
    suspend fun getGenres(@Query("limit") limit: Int = 25): JikanListResponse<GenreDto>

    @GET("anime/{id}")
    suspend fun getAnime(@Path("id") id: Int): JikanDataResponse<AnimeDto>

    @GET("anime/{id}/characters")
    suspend fun getCharacters(@Path("id") id: Int): JikanListResponse<CharacterWrapperDto>

    @GET("anime/{id}/videos")
    suspend fun getVideos(@Path("id") id: Int): JikanDataResponse<JikanVideosDto>

    @GET("anime/{id}/recommendations")
    suspend fun getRecommendations(@Path("id") id: Int): JikanListResponse<RecommendationDto>

    @GET("anime/{id}/relations")
    suspend fun getRelations(@Path("id") id: Int): JikanListResponse<RelationDto>

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 24,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("min_score") minScore: Double? = null,
        @Query("genres") genres: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("order_by") orderBy: String? = null,
        @Query("sort") sort: String? = "desc",
        @Query("sfw") sfw: Boolean = true,
    ): JikanListResponse<AnimeDto>
}
