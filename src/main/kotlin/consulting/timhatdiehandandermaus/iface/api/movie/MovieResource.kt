package consulting.timhatdiehandandermaus.iface.api.movie

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.usecase.AddMovie
import java.util.UUID
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response

@Path("/movie")
class MovieResource @Inject constructor(
    private val movieConverter: MovieConverter,
    private val addMovie: AddMovie,
    private val movieRepo: MovieRepository,
) {
    @PUT
    fun put(body: MoviePostRequest): MovieResponse {
        val movie = try {
            addMovie(body.imdbUrl)
        } catch (e: MovieNotFoundException) {
            throw NotFoundException(e)
        } catch (e: DuplicateMovieException) {
            throw WebApplicationException(e, Response.Status.CONFLICT)
        }
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

        val movie = movieRepo.find(uid) ?: throw NotFoundException("Not implemented ($uid)")
        return movieConverter.convertToResponse(movie)
    }
}
