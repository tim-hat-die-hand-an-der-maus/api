package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.value.MovieMetadataField
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException
import org.jboss.logging.Logger
import java.util.UUID

@RequestScoped
class RefreshMetadata @Inject constructor(
    private val log: Logger,
    private val metadataResolver: MovieMetadataResolver,
    private val movieRepo: MovieRepository,
) {
    operator fun invoke(movieId: UUID, fields: List<MovieMetadataField>) {
        val movie = movieRepo.find(movieId) ?: throw NotFoundException()
        log.info("Refreshing metadata for movie ${movie.metadata.title}")
        if (fields.isNotEmpty()) {
            // Doesn't matter which are requested since the source is always the same right now
            val metadata = metadataResolver.resolveImdbById(movie.metadata.id)
            if (metadata != movie.metadata) {
                log.info("Detected changes, updating metadata ($metadata)")
                movieRepo.updateMetadata(movie.id, metadata)
            }
        }
    }
}
