package consulting.timhatdiehandandermaus.iface.api.model

import consulting.timhatdiehandandermaus.domain.model.Movie
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

data class MovieMetadataResponse(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
    val coverUrl: String?,
)

@Mapper(uses = [UuidMapper::class])
interface MovieResponseConverter {

    @Mapping(source = "metadata", target = "imdb")
    fun convertToResponse(movie: Movie): MovieResponse
}
