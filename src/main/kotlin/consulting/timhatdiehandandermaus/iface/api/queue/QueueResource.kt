package consulting.timhatdiehandandermaus.iface.api.queue

import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import consulting.timhatdiehandandermaus.application.usecase.RemoveFromQueue
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponse
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponseConverter
import consulting.timhatdiehandandermaus.iface.api.model.QueueResponse
import consulting.timhatdiehandandermaus.iface.api.model.QueueResponseConverter
import java.util.UUID
import javax.inject.Inject
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/queue")
class QueueResource @Inject constructor(
    private val removeFromQueue: RemoveFromQueue,
    private val queueRepository: QueueRepository,
    private val queueResponseConverter: QueueResponseConverter,
    private val movieResponseConverter: MovieResponseConverter,
) {
    @GET
    fun list(): QueueResponse {
        val queueItems = queueRepository.list().map(queueResponseConverter::convertToResponse)
        return QueueResponse(queueItems)
    }

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: String): MovieResponse {
        val uid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            // ID is not a UUID, so it's unknown
            throw NotFoundException()
        }

        val result = try {
            removeFromQueue(uid)
        } catch (e: MovieNotFoundException) {
            throw NotFoundException(e)
        }

        return movieResponseConverter.convertToResponse(result)
    }
}
