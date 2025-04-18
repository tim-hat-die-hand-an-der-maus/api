package consulting.timhatdiehandandermaus.application.model

import java.time.Instant

data class CoverMetadata(
    val url: String,
    val ratio: Double,
)

data class MovieMetadata(
    /** IMDb/TMDB ID */
    val id: String,
    val type: MetadataSourceType,
    val title: String,
    val year: Int,
    val rating: String,
    val cover: CoverMetadata?,
    val infoPageUrl: String,
    val updateTime: Instant,
)
