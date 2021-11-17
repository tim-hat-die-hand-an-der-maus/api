package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDtoConverter
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class AddMovie @Inject constructor(
    private val metadataResolver: MovieMetadataResolver,
    private val converter: MovieInsertDtoConverter,
    private val movieRepo: MovieRepository,
) {

    @Throws(DuplicateMovieException::class)
    operator fun invoke(imdbUrl: String): Movie {
        // TODO error handling
        val metadata = metadataResolver.resolveImdb(imdbUrl)
        val movieDto = MovieInsertDto(
            status = MovieStatus.Queued,
            metadata = metadata,
        )
        // TODO handle duplicates
        val id = movieRepo.insert(movieDto)
        return converter.toMovie(id, movieDto)
    }
}
