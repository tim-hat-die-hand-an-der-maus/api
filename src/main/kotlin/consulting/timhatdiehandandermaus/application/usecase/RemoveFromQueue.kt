package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import java.util.UUID

@RequestScoped
class RemoveFromQueue
    @Inject
    constructor(
        private val movieRepo: MovieRepository,
        private val queueRepo: QueueRepository,
    ) {
        @Throws(MovieNotFoundException::class)
        operator fun invoke(
            movieId: UUID,
            status: MovieStatus,
        ): Movie {
            if (status !in listOf(MovieStatus.Deleted, MovieStatus.Watched)) {
                throw IllegalArgumentException()
            }
            queueRepo.delete(movieId)
            movieRepo.updateStatus(movieId, status)
            return movieRepo.find(movieId) ?: throw MovieNotFoundException()
        }
    }
