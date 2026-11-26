package com.project.destronix.data.remote.mapper

import com.project.destronix.data.remote.dto.AnimeDto
import com.project.destronix.data.remote.dto.CharacterWrapperDto
import com.project.destronix.data.remote.dto.GenreDto
import com.project.destronix.data.remote.dto.JikanImages
import com.project.destronix.data.remote.dto.JikanTrailer
import com.project.destronix.data.remote.dto.JikanTrailerImages
import com.project.destronix.data.remote.dto.PromoDto
import com.project.destronix.data.remote.dto.RelationDto
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeCharacter
import com.project.destronix.domain.model.AnimeGenre
import com.project.destronix.domain.model.AnimeRelation
import com.project.destronix.domain.model.AnimeVideo

private fun JikanImages?.bestUrl(): String? = this?.webp?.largeImageUrl
    ?: this?.jpg?.largeImageUrl
    ?: this?.jpg?.imageUrl

private fun JikanImages?.bestLargeUrl(): String? = this?.webp?.maximumImageUrl
    ?: this?.jpg?.largeImageUrl
    ?: bestUrl()

private fun JikanTrailerImages?.bestUrl(): String? = this?.maximumImageUrl ?: this?.largeImageUrl

fun AnimeDto.toDomain(): Anime = Anime(
    id = malId,
    title = title,
    titleEnglish = titleEnglish,
    imageUrl = images.bestUrl(),
    largeImageUrl = images.bestLargeUrl(),
    score = score,
    episodes = episodes,
    status = status,
    type = type,
    year = year,
    season = season,
    genres = genres.map { it.name },
    synopsis = synopsis,
    studios = studios.map { it.name },
    popularity = popularity,
    rank = rank,
    trailerUrl = trailer?.youtubeId?.let { "https://www.youtube.com/watch?v=$it" } ?: trailer?.url,
)

fun GenreDto.toDomain(): AnimeGenre = AnimeGenre(id = malId, name = name, count = count)

fun CharacterWrapperDto.toDomain(): AnimeCharacter = AnimeCharacter(
    id = character.malId,
    name = character.name,
    role = role,
    imageUrl = character.images.bestUrl(),
)

fun PromoDto.toDomain(): AnimeVideo? = trailer?.youtubeId?.takeIf { it.isNotBlank() }?.let {
    AnimeVideo(
        title = title?.takeIf { value -> value.isNotBlank() } ?: "Tráiler",
        youtubeId = it,
        thumbnailUrl = trailer?.images.bestUrl(),
    )
}

fun RelationDto.toDomain(): AnimeRelation = AnimeRelation(
    relation = relation,
    entries = entry.map { it.toDomain() },
)
