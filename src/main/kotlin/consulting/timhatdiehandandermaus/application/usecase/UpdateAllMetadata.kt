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
        operator fun invoke(cutoffDate: Instant? = null) {
            if (cutoffDate != null) {
                log.info("Updating metadata for all movies")
            } else {
                log.info("Updating metadata for all movies not updated since $cutoffDate")
            }

            movieRepo.forEachMovie(cutoffDate) { movie ->
                log.info("Resolving metadata for ${movie.metadata.title} (${movie.id})")
                val newMetadata = metadataResolver.resolveImdbById(movie.metadata.id)
                if (newMetadata != movie.metadata) {
                    log.info("Updating metadata ($newMetadata)")
                    movieRepo.updateMetadata(movie.id, newMetadata)
                }
            }
        }
    }
