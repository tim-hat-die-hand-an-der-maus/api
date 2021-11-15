package consulting.timhatdiehandandermaus.iface.api.queue

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("/queue")
class QueueResource {
    @GET
    fun list(): QueueResponse {
        return QueueResponse(emptyList())
    }
}
