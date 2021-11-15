package consulting.timhatdiehandandermaus.iface.api.queue

import MovieStatus
import java.util.UUID

data class QueueResponse(
    val queue: List<QueueItemResponse>,
)

data class QueueItemResponse(
    val id: UUID,
    val status: MovieStatus,
)
