package consulting.timhatdiehandandermaus.iface.api.movie

import consulting.timhatdiehandandermaus.application.usecase.AddMovie
import java.util.UUID
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/movie")
class MovieResource @Inject constructor(
    private val movieConverter: MovieConverter,
    private val addMovie: AddMovie,
) {
    @PUT
    fun put(body: MoviePostRequest): MovieResponse {
        val movie = addMovie(body.imdbUrl)
        return movieConverter.convertToResponse(movie)
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: String): MovieResponse {
        val uid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            // ID is not a UUID, so it's unknown
            throw NotFoundException()
        }
        // TODO: implement
        throw NotFoundException("Not implemented ($uid)")
    }
}
