package consulting.timhatdiehandandermaus.iface.api

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import java.net.URI

@Path("/")
class RootResource {
    @GET
    fun get(): Response = Response.temporaryRedirect(URI.create("/docs/swagger")).build()
}
