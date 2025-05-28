package consulting.timhatdiehandandermaus.iface.api.model

import consulting.timhatdiehandandermaus.application.repository.QueueItemDto
import consulting.timhatdiehandandermaus.iface.api.mapper.UuidMapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.util.UUID

data class QueueResponse(
    val queue: List<QueueItemResponse>,
)

data class QueueItemResponse(
    val id: String,
    val userId: UUID?,
)

@Mapper(uses = [UuidMapper::class])
interface QueueResponseConverter {
    @Mapping(source = "movieId", target = "id")
    fun convertToResponse(queue: QueueItemDto): QueueItemResponse
}
