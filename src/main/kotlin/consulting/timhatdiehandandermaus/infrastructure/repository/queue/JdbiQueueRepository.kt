package consulting.timhatdiehandandermaus.infrastructure.repository.queue

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.QueueItemDto
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.inTransactionUnchecked
import org.jdbi.v3.core.kotlin.useTransactionUnchecked
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.kotlin.attach
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.mapstruct.Mapper
import org.postgresql.util.PSQLException
import java.io.IOException
import java.util.UUID
import kotlin.collections.map

@RequestScoped
class JdbiQueueRepository
    @Inject
    constructor(
        private val jdbi: Jdbi,
        private val mapper: QueueItemRowMapper,
    ) : QueueRepository {
        @Throws(DuplicateMovieException::class)
        override fun insert(
            movieId: UUID,
            userId: UUID?,
        ) {
            jdbi.useTransactionUnchecked { handle ->
                val dao = handle.attach<QueueItemDao>()
                val lastIndex = dao.findLatestIndex()
                val nextIndex = lastIndex?.let { it + 1 } ?: 0

                try {
                    dao.insert(movieId = movieId, userId = userId, index = nextIndex)
                } catch (e: UnableToExecuteStatementException) {
                    val cause = e.cause
                    if (cause is PSQLException) {
                        when (cause.sqlState) {
                            "23503" -> throw MovieNotFoundException()
                            "23505" -> throw DuplicateMovieException(movieId)
                        }
                    }
                    throw IOException(e)
                }
            }
        }

        override fun list(): List<QueueItemDto> =
            jdbi.withHandleUnchecked { handle ->
                val rows = handle.attach<QueueItemDao>().list()
                rows.map(mapper::toModel)
            }

        @Throws(MovieNotFoundException::class)
        override fun delete(movieId: UUID) {
            val isDeleted =
                jdbi.inTransactionUnchecked { handle ->
                    handle.attach<QueueItemDao>().deleteById(movieId)
                }
            if (!isDeleted) {
                throw MovieNotFoundException()
            }
        }

        override fun updateOrder(movieIds: List<UUID>) =
            jdbi.useTransactionUnchecked { handle ->
                val dao = handle.attach<QueueItemDao>()
                movieIds.forEachIndexed { index, id ->
                    dao.updateIndex(id, index)
                }
            }
    }

private interface QueueItemDao {
    @SqlQuery("SELECT index FROM queue_item order by index desc limit 1")
    fun findLatestIndex(): Int?

    @SqlUpdate(
        """
            insert into queue_item (movie_id, user_id, index)
            values (:movieId, :userId, :index)
        """,
    )
    fun insert(
        movieId: UUID,
        userId: UUID?,
        index: Int,
    )

    @SqlUpdate("delete from queue_item where movie_id = :movieId")
    fun deleteById(movieId: UUID): Boolean

    @SqlQuery("select movie_id, user_id from queue_item order by index asc")
    fun list(): List<QueueItemRow>

    @SqlUpdate("update queue_item set index = :index - 1 where movie_id > :movieId")
    fun updateIndex(
        movieId: UUID,
        index: Int,
    )
}

@Mapper
interface QueueItemRowMapper {
    fun toModel(row: QueueItemRow): QueueItemDto
}

@RegisterForReflection
data class QueueItemRow(
    val movieId: UUID,
    val userId: UUID?,
)
