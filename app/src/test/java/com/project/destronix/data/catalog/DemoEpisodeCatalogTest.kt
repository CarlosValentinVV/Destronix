package com.project.destronix.data.catalog

import com.project.destronix.data.config.VideoServerConfig
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.VideoServerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoEpisodeCatalogTest {

    private val anime = Anime(id = 0, title = "Naruto", episodes = 2)

    @Test
    fun `configured servers produce one url per episode`() {
        val catalog = DemoEpisodeCatalog(
            streamBaseUrl = "https://demo.test/",
            servers = listOf(
                VideoServerConfig(id = "a", name = "Servidor A", baseUrl = "https://a.test/videos"),
                VideoServerConfig(
                    id = "b",
                    name = "Servidor B",
                    baseUrl = "https://b.test/ep/{episode}/file.m3u8",
                    type = VideoServerType.HLS,
                    language = "SUB",
                ),
            ),
        )

        val episodes = catalog.forAnime(anime)

        assertEquals(2, episodes.size)
        assertEquals(2, episodes.first().servers.size)
        assertEquals("https://a.test/videos/BigBuckBunny.mp4", episodes[0].servers[0].url)
        assertEquals("https://b.test/ep/1/file.m3u8", episodes[0].servers[1].url)
        assertEquals(VideoServerType.HLS, episodes[0].servers[1].type)
        assertEquals("SUB", episodes[0].servers[1].language)
        assertEquals(episodes[0].servers[0].url, episodes[0].streamUrl)
        assertEquals("https://a.test/videos/ElephantsDream.mp4", episodes[1].servers[0].url)
        assertEquals("https://b.test/ep/2/file.m3u8", episodes[1].servers[1].url)
    }

    @Test
    fun `embed servers are ignored and a demo server is used when none is playable`() {
        val onlyEmbed = DemoEpisodeCatalog(
            streamBaseUrl = "https://demo.test",
            servers = listOf(
                VideoServerConfig(id = "web", name = "Web", baseUrl = "https://web.test", type = VideoServerType.EMBED),
            ),
        )

        val episode = onlyEmbed.forAnime(anime).first()

        assertEquals(1, episode.servers.size)
        assertEquals("Demo", episode.servers.first().label)
        assertEquals("https://demo.test/BigBuckBunny.mp4", episode.servers.first().url)
        assertFalse(episode.servers.any { it.type == VideoServerType.EMBED })
    }

    @Test
    fun `urlFor falls back to the legacy stream url`() {
        val episode = com.project.destronix.domain.model.VideoEpisode(
            number = 1,
            title = "Episodio 1",
            streamUrl = "https://legacy.test/video.mp4",
        )

        assertEquals("https://legacy.test/video.mp4", episode.urlFor(0))
        assertEquals("https://legacy.test/video.mp4", episode.urlFor(4))
        assertTrue(episode.servers.isEmpty())
    }
}
