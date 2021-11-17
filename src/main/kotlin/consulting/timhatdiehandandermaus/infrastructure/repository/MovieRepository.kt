package consulting.timhatdiehandandermaus.infrastructure.repository

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDtoConverter
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class MemoryMovieRepository @Inject constructor(
    private val converter: MovieInsertDtoConverter
) : MovieRepository {
    private val db: MutableMap<UUID, Movie> = mutableMapOf()

    @Throws(DuplicateMovieException::class)
    override fun insert(movie: MovieInsertDto): UUID {
        if (db.values.any { it.metadata.id == movie.metadata.id }) {
            throw DuplicateMovieException("Movie with imdb ID ${movie.metadata.id} already exists")
        }

        val id = UUID.randomUUID()
        db[id] = converter.toMovie(id, movie)
        return id
    }

    @Throws(MovieNotFoundException::class)
    override fun updateMetadata(id: UUID, metadata: MovieMetadata) {
        val movie = db[id] ?: throw MovieNotFoundException("Movie with ID $id not found")
        movie.metadata = metadata
    }

    override fun find(id: UUID): Movie? {
        return db[id]
    }
}
