package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDtoConverter
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class AddMovie @Inject constructor(
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
        val id = movieRepo.insert(movieDto)
        queueRepo.insert(id)
        return converter.toMovie(id, movieDto)
    }
}
