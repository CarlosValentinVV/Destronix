package com.project.destronix.data.catalog

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.project.destronix.domain.model.Anime
import java.io.IOException

/**
 * Catálogo local de prueba. Los elementos se leen de `assets/demo_animes.json`,
 * así que puedes editarlos sin tocar Kotlin (sí hay que recompilar el APK).
 */
object DemoAnimeCatalog {

    private const val ASSET_NAME = "demo_animes.json"
    private val gson = Gson()

    fun load(context: Context): List<Anime> = try {
        context.assets.open(ASSET_NAME).bufferedReader().use { parse(it.readText()) }
    } catch (_: IOException) {
        fallback()
    }

    fun parse(json: String): List<Anime> = try {
        gson.fromJson(json, Array<Anime>::class.java)
            .orEmpty()
            .toList()
            .filter { it.id != 0 && it.title.isNotBlank() }
            .ifEmpty { fallback() }
    } catch (_: JsonSyntaxException) {
        fallback()
    }

    private fun fallback() = listOf(
        Anime(
            id = 9001,
            title = "Neon Rider",
            titleEnglish = "Neon Rider: Awakening",
            score = 8.7,
            episodes = 12,
            status = "Currently Airing",
            type = "TV",
            year = 2026,
            genres = listOf("Action", "Ciencia ficcion"),
            synopsis = "Un repartidor nocturno descubre una red de memoria prohibida.",
        ),
        Anime(
            id = 9003,
            title = "Cocina Celestial",
            score = 7.9,
            episodes = 12,
            status = "Finished Airing",
            type = "TV",
            year = 2024,
            genres = listOf("Comedia", "Recuentos de la vida"),
            synopsis = "Un chef cocina para dioses que solo aceptan platos imposibles.",
        ),
        Anime(
            id = 9005,
            title = "Mareas de cristal",
            score = 8.5,
            episodes = 13,
            status = "Currently Airing",
            type = "TV",
            year = 2026,
            genres = listOf("Fantasia", "Supernatural"),
            synopsis = "Cada marea devuelve un recuerdo que no pertenece a nadie vivo.",
        ),
    )
}
