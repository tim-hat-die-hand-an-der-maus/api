package consulting.timhatdiehandandermaus.iface.api.movie

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.usecase.AddMovie
import consulting.timhatdiehandandermaus.application.usecase.ListMovies
import consulting.timhatdiehandandermaus.iface.api.model.MovieGetStatus
import consulting.timhatdiehandandermaus.iface.api.model.MoviePostRequest
import consulting.timhatdiehandandermaus.iface.api.model.MovieRequestConverter
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponse
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponseConverter
import consulting.timhatdiehandandermaus.iface.api.model.MoviesResponse
import io.quarkus.security.Authenticated
import jakarta.annotation.security.PermitAll
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.jboss.logging.Logger
import java.util.UUID

@Path("/movie")
@RequestScoped
@Authenticated
class MovieResource
    @Inject
    constructor(
        private val log: Logger,
        private val movieConverter: MovieResponseConverter,
        private val movieRequestConverter: MovieRequestConverter,
        private val addMovie: AddMovie,
        private val listMovies: ListMovies,
        private val movieRepo: MovieRepository,
    ) {
        @PUT
        fun put(body: MoviePostRequest): MovieResponse {
            val movie =
                try {
                    addMovie(body.imdbUrl, userId = body.userId)
                } catch (e: MovieNotFoundException) {
                    throw NotFoundException(e)
                } catch (e: DuplicateMovieException) {
                    throw WebApplicationException(e, Response.Status.CONFLICT)
                }
            return movieConverter.convertToResponse(movie)
        }

        @GET
        @Path("/{id}")
        @PermitAll
        @Operation(summary = "Get a specific movie using its ID")
        fun get(
            @PathParam("id") id: String,
        ): MovieResponse {
            val uid =
                try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    // ID is not a UUID, so it's unknown
                    throw NotFoundException()
                }

            val movie = movieRepo.find(uid) ?: throw NotFoundException("Not implemented ($uid)")
            return movieConverter.convertToResponse(movie)
        }

        @GET
        @Path("/")
        @PermitAll
        @Operation(
            summary = "Query the list of known movies.",
        )
        fun get(
            @QueryParam("q") query: String?,
            @QueryParam("status") status: MovieGetStatus?,
        ): MoviesResponse {
            val domainStatus = status?.let(movieRequestConverter::toMovieStatus)
            val movies = listMovies(query, domainStatus)
            return MoviesResponse(
                movies = movies.map(movieConverter::convertToResponse),
            )
        }
    }
