package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.port.MetadataSource
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
        @MetadataSource(MetadataSourceType.IMDB)
        private val imdbResolver: MovieMetadataResolver,
        @MetadataSource(MetadataSourceType.TMDB)
        private val tmdbResolver: MovieMetadataResolver,
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
                log.info("Resolving IMDb metadata for ${movie.imdbMetadata.title} (${movie.id})")
                val imdbMetadata = imdbResolver.resolveById(movie.imdbMetadata.id)
                if (!imdbMetadata.equalsIgnoreUpdateTime(movie.imdbMetadata)) {
                    log.info("Updating metadata ($imdbMetadata)")
                    movieRepo.updateMetadata(movie.id, imdbMetadata)
                }

                log.info("Resolving TMDB metadata for ${movie.imdbMetadata.title} (${movie.id})")
                val oldMetadata = movie.tmdbMetadata

                val tmdbMetadata =
                    try {
                        if (oldMetadata == null) {
                            tmdbResolver.resolveById(imdbMetadata.id, MetadataSourceType.IMDB)
                        } else {
                            tmdbResolver.resolveById(oldMetadata.id)
                        }
                    } catch (e: MovieNotFoundException) {
                        log.error("Failed to resolve TMDB metadata for ${imdbMetadata.title} (IMDb ${imdbMetadata.id})", e)
                        null
                    }

                if (tmdbMetadata != null && (oldMetadata == null || !tmdbMetadata.equalsIgnoreUpdateTime(oldMetadata))) {
                    log.info("Updating metadata ($tmdbMetadata)")
                    movieRepo.updateMetadata(movie.id, tmdbMetadata)
                }
            }
        }
    }
