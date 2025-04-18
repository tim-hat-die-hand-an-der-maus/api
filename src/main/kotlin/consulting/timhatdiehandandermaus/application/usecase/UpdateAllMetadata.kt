package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.Instant

@RequestScoped
class UpdateAllMetadata
    @Inject
    constructor(
        private val log: Logger,
        private val metadataResolver: MovieMetadataResolver,
        private val movieRepo: MovieRepository,
    ) {
        operator fun invoke(
            cutoffDate: Instant? = null,
            limit: Long = 0,
        ) {
            if (cutoffDate != null) {
                log.info("Updating metadata for all movies")
            } else {
                log.info("Updating metadata for all movies not updated since $cutoffDate")
            }

            movieRepo.forEachMovie(cutoffDate, limit = limit) { movie ->
                log.info("Resolving metadata for ${movie.imdbMetadata.title} (${movie.id})")
                val newMetadata = metadataResolver.resolveImdbById(movie.imdbMetadata.id)
                if (newMetadata != movie.imdbMetadata) {
                    log.info("Updating metadata ($newMetadata)")
                    movieRepo.updateMetadata(movie.id, newMetadata)
                }
            }
        }
    }
