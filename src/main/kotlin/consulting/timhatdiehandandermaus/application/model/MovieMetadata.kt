package consulting.timhatdiehandandermaus.application.model

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant

@RegisterForReflection
data class CoverMetadata(
    val url: String,
    val ratio: Double,
)

@RegisterForReflection
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
) {
    fun equalsIgnoreUpdateTime(other: MovieMetadata): Boolean = copy(updateTime = Instant.EPOCH) == other.copy(updateTime = Instant.EPOCH)
}
