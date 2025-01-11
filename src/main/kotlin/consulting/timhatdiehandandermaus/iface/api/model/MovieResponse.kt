package consulting.timhatdiehandandermaus.iface.api.model

import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
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
    @Deprecated("Use cover object instead")
    val coverUrl: String,
)

@Mapper(uses = [UuidMapper::class])
interface MovieResponseConverter {
    @Mapping(source = "metadata", target = "imdb")
    fun convertToResponse(movie: Movie): MovieResponse

    @Mapping(expression = "java(cover.getUrl())", target = "coverUrl")
    fun convertToResponse(movieMetadata: MovieMetadata): MovieMetadataResponse
}
