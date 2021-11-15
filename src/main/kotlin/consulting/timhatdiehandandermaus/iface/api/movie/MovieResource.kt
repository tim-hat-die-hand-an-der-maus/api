package consulting.timhatdiehandandermaus.iface.api.movie

import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import java.util.UUID
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/movie")
class MovieResource {
    @PUT
    fun put(body: MoviePostRequest): MovieResponse {
        // TODO: implement
        return MovieResponse(
            UUID.randomUUID(),
            MovieStatus.Queued,
            ImdbMetadata(
                "1234567",
                "Movie Title",
                2021,
                "10.0",
            ),
        )
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: String): MovieResponse {
        val uid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            // ID is not an UUID, so it's unknown
            throw NotFoundException()
        }
        // TODO: implement
        throw NotFoundException("Not implemented ($uid)")
    }
}
