package consulting.timhatdiehandandermaus.iface.api.queue

import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import consulting.timhatdiehandandermaus.application.usecase.RemoveFromQueue
import consulting.timhatdiehandandermaus.iface.api.model.MovieDeleteStatus
import consulting.timhatdiehandandermaus.iface.api.model.MovieRequestConverter
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponse
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponseConverter
import consulting.timhatdiehandandermaus.iface.api.model.QueueResponse
import consulting.timhatdiehandandermaus.iface.api.model.QueueResponseConverter
import io.quarkus.security.Authenticated
import jakarta.annotation.security.PermitAll
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import java.util.UUID

@Path("/queue")
@RequestScoped
@Authenticated
class QueueResource
    @Inject
    constructor(
        private val removeFromQueue: RemoveFromQueue,
        private val queueRepository: QueueRepository,
        private val queueResponseConverter: QueueResponseConverter,
        private val movieRequestConverter: MovieRequestConverter,
        private val movieResponseConverter: MovieResponseConverter,
    ) {
        @GET
        @PermitAll
        fun list(): QueueResponse {
            val queueItems = queueRepository.list().map(queueResponseConverter::convertToResponse)
            return QueueResponse(queueItems)
        }

        @DELETE
        @Path("/{id}")
        fun delete(
            @PathParam("id")
            id: String,
            @QueryParam("status")
            @DefaultValue("Deleted")
            status: MovieDeleteStatus,
        ): MovieResponse {
            val uid =
                try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    // ID is not a UUID, so it's unknown
                    throw NotFoundException()
                }

            val movieStatus = movieRequestConverter.toMovieStatus(status)

            val result =
                try {
                    removeFromQueue(uid, movieStatus)
                } catch (e: MovieNotFoundException) {
                    throw NotFoundException(e)
                }

            return movieResponseConverter.convertToResponse(result)
        }
    }
