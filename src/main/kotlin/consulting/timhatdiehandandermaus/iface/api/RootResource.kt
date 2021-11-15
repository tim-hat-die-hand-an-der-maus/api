package consulting.timhatdiehandandermaus.iface.api

import java.net.URI
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response

@Path("/")
class RootResource {
    @GET
    fun get(): Response {
        return Response.temporaryRedirect(URI.create("/docs/swagger")).build()
    }
}
