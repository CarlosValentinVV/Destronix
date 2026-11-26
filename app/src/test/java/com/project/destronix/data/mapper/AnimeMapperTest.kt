package com.project.destronix.data.mapper

import com.project.destronix.data.remote.dto.AnimeDto
import com.project.destronix.data.remote.dto.JikanImageSet
import com.project.destronix.data.remote.dto.JikanImages
import com.project.destronix.data.remote.dto.JikanNamedResource
import com.project.destronix.data.remote.dto.JikanTrailer
import com.project.destronix.data.remote.dto.JikanTrailerImages
import com.project.destronix.data.remote.dto.PromoDto
import com.project.destronix.data.remote.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnimeMapperTest {

    @Test
    fun `anime DTO is mapped with webp image and english title`() {
        val dto = AnimeDto(
            malId = 1,
            title = "Título original",
            titleEnglish = "English title",
            images = JikanImages(
                jpg = JikanImageSet(imageUrl = "jpg.jpg", largeImageUrl = "jpg-large.jpg"),
                webp = JikanImageSet(imageUrl = "webp.webp", largeImageUrl = "webp-large.webp"),
            ),
            genres = listOf(JikanNamedResource(malId = 1, name = "Acción")),
            studios = listOf(JikanNamedResource(malId = 2, name = "MAPPA")),
            trailer = JikanTrailer(youtubeId = "abc123"),
        )

        val anime = dto.toDomain()

        assertEquals("English title", anime.displayTitle)
        assertEquals("webp-large.webp", anime.imageUrl)
        assertEquals(listOf("Acción"), anime.genres)
        assertEquals(listOf("MAPPA"), anime.studios)
        assertEquals("https://www.youtube.com/watch?v=abc123", anime.trailerUrl)
    }

    @Test
    fun `anime without english title falls back to original title`() {
        val anime = AnimeDto(malId = 2, title = "Original").toDomain()

        assertEquals("Original", anime.displayTitle)
        assertNull(anime.imageUrl)
        assertNull(anime.trailerUrl)
    }

    @Test
    fun `promo without youtube id is discarded`() {
        assertNull(PromoDto(title = "Trailer", trailer = null).toDomain())
    }

    @Test
    fun `promo is mapped with its thumbnail`() {
        val video = PromoDto(
            title = "PV 1",
            trailer = JikanTrailer(
                youtubeId = "xyz",
                images = JikanTrailerImages(maximumImageUrl = "thumb.jpg"),
            ),
        ).toDomain()

        assertEquals("PV 1", video?.title)
        assertEquals("xyz", video?.youtubeId)
        assertEquals("thumb.jpg", video?.thumbnailUrl)
    }
}
