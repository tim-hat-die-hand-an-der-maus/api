package consulting.timhatdiehandandermaus.application.repository

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import java.util.UUID

data class QueueItemDto(
    val movieId: UUID,
    val userId: UUID?,
    // more info about votes and stuff
)

interface QueueRepository {
    @Throws(DuplicateMovieException::class)
    fun insert(
        movieId: UUID,
        userId: UUID?,
    )

    fun list(): List<QueueItemDto>

    @Throws(MovieNotFoundException::class)
    fun delete(movieId: UUID)

    fun updateOrder(movieIds: List<UUID>)
}
