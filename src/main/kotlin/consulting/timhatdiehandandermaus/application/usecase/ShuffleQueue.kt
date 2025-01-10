package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

@RequestScoped
class ShuffleQueue
    @Inject
    constructor(
        val log: Logger,
        val queueRepository: QueueRepository,
    ) {
        operator fun invoke() {
            val movies = queueRepository.list()
            val shuffled = movies.map { it.movieId }.shuffled()
            queueRepository.updateOrder(shuffled)
            log.info("Successfully shuffled the queue")
        }
    }
