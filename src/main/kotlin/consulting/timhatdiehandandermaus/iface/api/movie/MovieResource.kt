package consulting.timhatdiehandandermaus.iface.api.movie

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.usecase.AddMovie
import consulting.timhatdiehandandermaus.application.usecase.ListMovies
import consulting.timhatdiehandandermaus.application.usecase.RefreshMetadata
import consulting.timhatdiehandandermaus.iface.api.model.MovieGetStatus
import consulting.timhatdiehandandermaus.iface.api.model.MovieMetadataField
import consulting.timhatdiehandandermaus.iface.api.model.MovieMetadataFieldConverter
import consulting.timhatdiehandandermaus.iface.api.model.MovieMetadataPatchRequest
import consulting.timhatdiehandandermaus.iface.api.model.MoviePostRequest
import consulting.timhatdiehandandermaus.iface.api.model.MovieRequestConverter
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponse
import consulting.timhatdiehandandermaus.iface.api.model.MovieResponseConverter
import consulting.timhatdiehandandermaus.iface.api.model.MoviesResponse
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.PATCH
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
class MovieResource @Inject constructor(
    private val log: Logger,
    private val movieConverter: MovieResponseConverter,
    private val movieRequestConverter: MovieRequestConverter,
    private val addMovie: AddMovie,
    private val listMovies: ListMovies,
    private val refreshMetadata: RefreshMetadata,
    private val movieMetadataFieldConverter: MovieMetadataFieldConverter,
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
    @Operation(summary = "Get a specific movie using its ID")
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

    @GET
    @Path("/")
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

    @PATCH
    @Path("/{id}/metadata")
    fun patchMetadata(@PathParam("id") id: String, body: MovieMetadataPatchRequest) {
        val uid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            // ID is not a UUID, so it's unknown
            throw NotFoundException()
        }

        val fields = body.refresh.map {
            if (it == MovieMetadataField.coverUrl) {
                log.warn("Got request with deprecated coverUrl field")
            }
            movieMetadataFieldConverter.toDomain(it)
        }
        refreshMetadata(uid, fields)
    }
}
