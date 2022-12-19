package consulting.timhatdiehandandermaus.infrastructure.repository.queue

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.repository.QueueItemDto
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityExistsException
import javax.persistence.Id
import javax.persistence.Table
import javax.transaction.Transactional

@RequestScoped
class SqlQueueRepository @Inject constructor(
    private val movieRepo: MovieRepository,
) : QueueRepository, PanacheRepositoryBase<QueueItemEntity, UUID> {
    @Transactional
    @Throws(DuplicateMovieException::class, MovieNotFoundException::class)
    override fun insert(movieId: UUID) {
        if (movieRepo.find(movieId) == null) {
            throw MovieNotFoundException()
        }

        if (findById(movieId) != null) {
            throw DuplicateMovieException(movieId)
        }

        val lastItem = find("", Sort.descending("index")).firstResult()
        val nextIndex = lastItem?.let { it.index + 1 } ?: 0
        try {
            persist(QueueItemEntity(movieId, nextIndex))
        } catch (e: EntityExistsException) {
            // It's okay!
            return
        }
    }

    override fun list(): List<QueueItemDto> {
        return list("", Sort.ascending("index"))
            .map { QueueItemDto(it.movieId) }
    }

    @Transactional
    @Throws(MovieNotFoundException::class)
    override fun delete(movieId: UUID) {
        if (!deleteById(movieId)) {
            throw MovieNotFoundException()
        }
    }

    @Transactional
    override fun updateOrder(movieIds: List<UUID>) {
        movieIds.forEachIndexed { index, id ->
            val entity = findById(id) ?: throw MovieNotFoundException()
            entity.index = index
            persist(entity)
        }
    }
}

@Entity
@Table(name = "queue_item")
class QueueItemEntity(
    @Id
    @Column(name = "movie_id")
    var movieId: UUID,
    @Column(nullable = false)
    var index: Int,
)
