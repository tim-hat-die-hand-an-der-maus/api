// ktlint-disable filename

package consulting.timhatdiehandandermaus.infrastructure.repository

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.QueueItemDto
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import java.util.Collections
import java.util.UUID
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MemoryQueueRepository : QueueRepository {
    private val db: MutableList<QueueItemDto> = mutableListOf()

    @Throws(MovieNotFoundException::class)
    override fun insert(movieId: UUID) {
        if (db.any { it.movieId == movieId }) {
            throw DuplicateMovieException("Movie with ID $movieId is already in queue")
        }

        db.add(QueueItemDto(movieId))
    }

    override fun list(): List<QueueItemDto> {
        return Collections.unmodifiableList(db)
    }

    @Throws(MovieNotFoundException::class)
    override fun delete(movieId: UUID) {
        if (!db.removeAll { it.movieId == movieId }) {
            throw MovieNotFoundException()
        }
    }
}
