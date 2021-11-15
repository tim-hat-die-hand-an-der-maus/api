package consulting.timhatdiehandandermaus.iface.api.queue

import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/queue")
class QueueResource {
    @GET
    fun list(): QueueResponse {
        // TODO: implement
        return QueueResponse(emptyList())
    }

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: String): Response {
        // TODO implement
        return Response.noContent().build()
    }
}
