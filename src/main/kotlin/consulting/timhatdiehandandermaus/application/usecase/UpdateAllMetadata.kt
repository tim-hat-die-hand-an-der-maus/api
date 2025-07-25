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
        @MetadataSource(MetadataSourceType.TMDB)
        private val tmdbResolver: MovieMetadataResolver,
        private val movieRepo: MovieRepository,
    ) {
        operator fun invoke(
            cutoffDate: Instant? = null,
            limit: Long = 0,
        ) {
            if (cutoffDate != null) {
                log.info("Updating metadata for all movies (limit=$limit)")
            } else {
                log.info(
                    "Updating metadata for all movies not updated since $cutoffDate (limit=$limit)",
                )
            }

            var count = 0
            movieRepo.forEachMovie(cutoffDate, limit = limit) { movie ->
                count += 1
                val oldImdbMetadata = movie.imdbMetadata

                log.info("Resolving TMDB metadata for movie ${movie.id}")
                val oldTmdbMetadata = movie.tmdbMetadata

                val tmdbMetadata =
                    try {
                        when {
                            oldTmdbMetadata == null && oldImdbMetadata != null ->
                                tmdbResolver.resolveById(
                                    oldImdbMetadata.id,
                                    MetadataSourceType.IMDB,
                                )
                            oldTmdbMetadata != null -> tmdbResolver.resolveById(oldTmdbMetadata.id)
                            else -> null
                        }
                    } catch (e: MovieNotFoundException) {
                        log.error("Failed to resolve TMDB metadata for ${movie.id}", e)
                        null
                    }

                if (tmdbMetadata != null && (oldTmdbMetadata == null || !tmdbMetadata.equalsIgnoreUpdateTime(oldTmdbMetadata))) {
                    log.info("Updating metadata ($tmdbMetadata)")
                    movieRepo.updateMetadata(movie.id, tmdbMetadata)
                }
            }

            log.info("Processed $count movies")
        }
    }
