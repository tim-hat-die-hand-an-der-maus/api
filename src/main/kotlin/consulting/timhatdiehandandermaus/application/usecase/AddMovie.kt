package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDtoConverter
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import org.jboss.logging.Logger
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class AddMovie @Inject constructor(
    private val log: Logger,
    private val metadataResolver: MovieMetadataResolver,
    private val converter: MovieInsertDtoConverter,
    private val movieRepo: MovieRepository,
    private val queueRepo: QueueRepository,
) {
    @Throws(DuplicateMovieException::class)
    operator fun invoke(imdbUrl: String): Movie {
        // TODO error handling
        val metadata = metadataResolver.resolveImdb(imdbUrl)
        val movieDto = MovieInsertDto(
            status = MovieStatus.Queued,
            metadata = metadata,
        )
        val id = try {
            movieRepo.insert(movieDto)
        } catch (e: DuplicateMovieException) {
            log.debug("Movie already exists in database, refreshing metadata")
            movieRepo.updateMetadata(e.id, metadata)
            e.id
        }
        queueRepo.insert(id)
        log.info("Inserted movie $id (${metadata.title}) into the database")
        return converter.toMovie(id, movieDto)
    }
}
