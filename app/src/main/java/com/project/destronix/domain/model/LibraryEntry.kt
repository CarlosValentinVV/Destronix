package com.project.destronix.domain.model

/** Estado de seguimiento de un anime en la biblioteca del usuario. */
enum class LibraryStatus(val label: String) {
    WATCHING("Viendo"),
    PLANNING("Pendiente"),
    COMPLETED("Completado"),
    PAUSED("Pausado"),
    DROPPED("Abandonado"),
    ;

    companion object {
        fun from(value: String): LibraryStatus =
            entries.firstOrNull { it.name == value } ?: PLANNING
    }
}

data class LibraryEntry(
    val animeId: Int,
    val title: String,
    val imageUrl: String?,
    val status: LibraryStatus = LibraryStatus.PLANNING,
    val score: Double? = null,
    val progressEpisodes: Int = 0,
    val totalEpisodes: Int? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val progress: Float
        get() = if (totalEpisodes != null && totalEpisodes > 0) {
            (progressEpisodes.toFloat() / totalEpisodes).coerceIn(0f, 1f)
        } else {
            0f
        }
}
