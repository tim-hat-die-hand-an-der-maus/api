package consulting.timhatdiehandandermaus.application.model

data class CoverMetadata(
    val url: String,
    val ratio: Double,
)

data class MovieMetadata(
    /** IMDb/TMDB ID */
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
    val cover: CoverMetadata?,
    val infoPageUrl: String?,
)
