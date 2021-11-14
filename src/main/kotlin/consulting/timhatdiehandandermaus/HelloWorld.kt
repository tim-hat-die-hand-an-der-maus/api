package consulting.timhatdiehandandermaus

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("/hello")
class HelloWorld {
    @GET
    fun hello() = Greeting("moin")
}
