package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import org.jboss.logging.Logger
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class FindMissingCovers @Inject constructor(
    private val log: Logger,
    private val metadataResolver: MovieMetadataResolver,
    private val movieRepo: MovieRepository,
) {
    operator fun invoke() {
        log.info("Looking for movies without cover URL")

        val movies = movieRepo.findWithoutCoverUrl()

        if (movies.isEmpty()) {
            log.info("No movies with missing covers found.")
            return
        }

        for (movie in movies) {
            val newMetadata = metadataResolver.resolveImdbById(movie.metadata.id)
            if (newMetadata != movie.metadata) {
                log.info("Updating metadata for ${movie.metadata.title}")
                movieRepo.updateMetadata(movie.id, newMetadata)
            }
        }
    }
}
