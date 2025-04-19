package consulting.timhatdiehandandermaus.iface.api.model

import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.iface.api.mapper.UuidMapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping

enum class MovieStatusResponse {
    Queued,
    Watched,
    Deleted,
}

data class MovieResponse(
    val id: String,
    val status: MovieStatusResponse,
    val imdb: MovieMetadataResponse,
    val tmdb: MovieMetadataResponse?,
)

data class MoviesResponse(
    val movies: List<MovieResponse>,
)

data class CoverMetadataResponse(
    val url: String,
    val ratio: Double,
)

data class MovieMetadataResponse(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
    val cover: CoverMetadataResponse,
    val infoPageUrl: String,
)

@Mapper(uses = [UuidMapper::class])
interface MovieResponseConverter {
    @Mapping(source = "imdbMetadata", target = "imdb")
    @Mapping(source = "tmdbMetadata", target = "tmdb")
    fun convertToResponse(movie: Movie): MovieResponse
}
