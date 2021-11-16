package consulting.timhatdiehandandermaus.iface.api.queue

import consulting.timhatdiehandandermaus.domain.model.MovieStatus

data class QueueResponse(
    val queue: List<QueueItemResponse>,
)

data class QueueItemResponse(
    val id: String,
    val status: MovieStatus,
)
