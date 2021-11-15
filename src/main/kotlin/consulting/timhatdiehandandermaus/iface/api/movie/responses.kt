package consulting.timhatdiehandandermaus.iface.api.movie

import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import java.util.UUID

data class MovieResponse(
    val id: UUID,
    val status: MovieStatus,
    val imdb: ImdbMetadata,
)

data class ImdbMetadata(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
)
