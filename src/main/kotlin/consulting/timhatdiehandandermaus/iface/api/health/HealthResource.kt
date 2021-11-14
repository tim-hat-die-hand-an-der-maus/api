package consulting.timhatdiehandandermaus.iface.api.health

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("/health")
class HealthResource {
    @GET
    fun get(): HealthResponse {
        return HealthResponse()
    }
}
