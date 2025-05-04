package consulting.timhatdiehandandermaus.application.repository

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.time.Instant
import java.util.UUID

data class MovieInsertDto(
    val status: MovieStatus,
    val imdbMetadata: MovieMetadata?,
    val tmdbMetadata: MovieMetadata?,
) {
    val availableMetadata: List<MovieMetadata>
        get() = listOfNotNull(imdbMetadata, tmdbMetadata)
}

@Mapper
interface MovieInsertDtoConverter {
    @Mapping(target = "id", source = "id")
    fun toMovie(
        id: UUID,
        dto: MovieInsertDto,
    ): Movie
}

interface MovieRepository {
    @Throws(DuplicateMovieException::class)
    fun insert(movie: MovieInsertDto): UUID

    @Throws(MovieNotFoundException::class)
    fun updateMetadata(
        id: UUID,
        metadata: MovieMetadata,
    )

    @Throws(MovieNotFoundException::class)
    fun updateStatus(
        id: UUID,
        status: MovieStatus,
    )

    fun find(id: UUID): Movie?

    fun forEachMovie(
        metadataUpdateTimeCutoff: Instant? = null,
        limit: Long = 0,
        action: (Movie) -> Unit,
    )

    fun listMovies(status: MovieStatus?): List<Movie>
}
