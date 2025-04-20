package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.port.MetadataSource
import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDtoConverter
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.util.UUID

private const val THAT_MOVIE_WITH_AN_AIRPLANE = "f388de4e-184e-4258-a0b5-10ad753c1ece"

@RequestScoped
class AddMovie
    @Inject
    constructor(
        private val log: Logger,
        @MetadataSource(MetadataSourceType.IMDB)
        private val imdbResolver: MovieMetadataResolver,
        @MetadataSource(MetadataSourceType.TMDB)
        private val tmdbResolver: MovieMetadataResolver,
        private val converter: MovieInsertDtoConverter,
        private val movieRepo: MovieRepository,
        private val queueRepo: QueueRepository,
    ) {
        @Throws(DuplicateMovieException::class)
        operator fun invoke(url: String): Movie {
            val imdbMetadata =
                try {
                    imdbResolver.resolveByUrl(url)
                } catch (e: MovieNotFoundException) {
                    log.info("Movie not found on IMDb: $url")
                    null
                }
            val tmdbMetadata =
                try {
                    tmdbResolver.resolveByUrl(url)
                } catch (e: MovieNotFoundException) {
                    log.info("Movie not found on TMDB: $url")
                    null
                }

            if (imdbMetadata == null && tmdbMetadata == null) {
                throw MovieNotFoundException()
            }

            val movieDto =
                MovieInsertDto(
                    status = MovieStatus.Queued,
                    imdbMetadata = imdbMetadata,
                    tmdbMetadata = tmdbMetadata,
                )
            val id =
                try {
                    movieRepo.insert(movieDto)
                } catch (e: DuplicateMovieException) {
                    log.debug("Movie already exists in database, refreshing metadata")
                    val movieId = e.id

                    if (imdbMetadata != null) {
                        movieRepo.updateMetadata(movieId, imdbMetadata)
                    }
                    if (tmdbMetadata != null) {
                        movieRepo.updateMetadata(movieId, tmdbMetadata)
                    }

                    if (movieId != UUID.fromString(THAT_MOVIE_WITH_AN_AIRPLANE)) {
                        throw e
                    }

                    movieRepo.updateStatus(movieId, MovieStatus.Queued)
                    movieId
                }
            queueRepo.insert(id)
            log.info("Inserted movie $id into the database")
            return converter.toMovie(id, movieDto)
        }
    }
