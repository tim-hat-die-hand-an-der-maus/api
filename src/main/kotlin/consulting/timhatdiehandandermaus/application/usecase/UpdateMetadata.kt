package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import org.jboss.logging.Logger
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class UpdateMetadata @Inject constructor(
    private val log: Logger,
    private val metadataResolver: MovieMetadataResolver,
    private val movieRepo: MovieRepository,
) {
    operator fun invoke() {
        log.info("Updating metadata for all movies")

        movieRepo.forEachMovie { movie ->
            log.info("Resolving metadata for ${movie.metadata.title} (${movie.id})")
            val newMetadata = metadataResolver.resolveImdbById(movie.metadata.id)
            if (newMetadata != movie.metadata) {
                log.info("Updating metadata ($newMetadata)")
                movieRepo.updateMetadata(movie.id, newMetadata)
            }
        }
    }
}
