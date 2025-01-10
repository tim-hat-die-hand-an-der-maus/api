package consulting.timhatdiehandandermaus.application.repository

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import org.mapstruct.Mapper
import java.util.UUID

data class MovieInsertDto(
    val status: MovieStatus,
    val metadata: MovieMetadata,
)

@Mapper
interface MovieInsertDtoConverter {
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

    fun forEachMovie(action: (Movie) -> Unit)

    fun listMovies(status: MovieStatus?): Iterable<Movie>
}
