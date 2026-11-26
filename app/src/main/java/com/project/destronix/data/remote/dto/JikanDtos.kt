package com.project.destronix.data.remote.dto

import com.google.gson.annotations.SerializedName

data class JikanListResponse<T>(
    val data: List<T> = emptyList(),
    val pagination: JikanPagination? = null,
)

data class JikanDataResponse<T>(val data: T)

data class JikanPagination(
    @SerializedName("has_next_page") val hasNextPage: Boolean = false,
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_visible_page") val lastVisiblePage: Int = 1,
)

data class JikanImages(
    val jpg: JikanImageSet? = null,
    val webp: JikanImageSet? = null,
)

data class JikanImageSet(
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("small_image_url") val smallImageUrl: String? = null,
    @SerializedName("large_image_url") val largeImageUrl: String? = null,
    @SerializedName("maximum_image_url") val maximumImageUrl: String? = null,
)

data class JikanTrailer(
    @SerializedName("youtube_id") val youtubeId: String? = null,
    val url: String? = null,
    val images: JikanTrailerImages? = null,
)

data class JikanTrailerImages(
    @SerializedName("maximum_image_url") val maximumImageUrl: String? = null,
    @SerializedName("large_image_url") val largeImageUrl: String? = null,
)

data class JikanNamedResource(
    @SerializedName("mal_id") val malId: Int = 0,
    val name: String = "",
    val type: String? = null,
)

data class AnimeDto(
    @SerializedName("mal_id") val malId: Int = 0,
    val url: String? = null,
    val images: JikanImages? = null,
    val trailer: JikanTrailer? = null,
    val title: String = "",
    @SerializedName("title_english") val titleEnglish: String? = null,
    val type: String? = null,
    val episodes: Int? = null,
    val status: String? = null,
    val score: Double? = null,
    val rank: Int? = null,
    val popularity: Int? = null,
    val season: String? = null,
    val year: Int? = null,
    val synopsis: String? = null,
    val background: String? = null,
    val duration: String? = null,
    val rating: String? = null,
    val source: String? = null,
    val broadcast: String? = null,
    val genres: List<JikanNamedResource> = emptyList(),
    val studios: List<JikanNamedResource> = emptyList(),
    val themes: List<JikanNamedResource> = emptyList(),
)

data class CharacterWrapperDto(
    val character: JikanCharacterDto,
    val role: String? = null,
)

data class JikanCharacterDto(
    @SerializedName("mal_id") val malId: Int = 0,
    val name: String = "",
    val images: JikanImages? = null,
)

data class JikanVideosDto(
    val promo: List<PromoDto> = emptyList(),
    val episodes: List<PromoDto> = emptyList(),
)

data class PromoDto(
    val title: String? = null,
    val trailer: JikanTrailer? = null,
)

data class RecommendationDto(val entry: AnimeDto)

data class RelationDto(
    val relation: String = "",
    val entry: List<AnimeDto> = emptyList(),
)

data class GenreDto(
    @SerializedName("mal_id") val malId: Int = 0,
    val name: String = "",
    val count: Int? = null,
)
