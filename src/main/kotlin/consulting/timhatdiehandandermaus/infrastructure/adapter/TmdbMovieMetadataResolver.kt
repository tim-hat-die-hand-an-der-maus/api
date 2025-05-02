package consulting.timhatdiehandandermaus.infrastructure.adapter

import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.port.MetadataSource
import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.io.IOException

data class TmdbUrlRequest(
    val link: String,
)

data class TmdbCoverResponse(
    val url: String,
    val ratio: Double,
)

data class TmdbResponse(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String?,
    val cover: TmdbCoverResponse?,
    val imdbUrl: String?,
    val tmdbUrl: String,
)

@RegisterRestClient(configKey = "tmdb-api")
interface TmdbService {
    @POST
    @Path("/by_link")
    fun resolveMetadata(request: TmdbUrlRequest): TmdbResponse

    @POST
    @Path("/by_id/{movieId}")
    fun resolveMetadataById(
        @PathParam("movieId") movieId: String,
        @QueryParam("external_source") externalSource: String?,
    ): TmdbResponse
}

@Mapper
interface TmdbResponseConverter {
    @Mapping(target = "infoPageUrl", source = "tmdbUrl")
    @Mapping(target = "type", expression = "java(consulting.timhatdiehandandermaus.application.model.MetadataSourceType.TMDB)")
    @Mapping(target = "updateTime", expression = "java(java.time.Instant.now())")
    fun toModel(response: TmdbResponse): MovieMetadata
}

@MetadataSource(MetadataSourceType.TMDB)
@RequestScoped
class TmdbMovieMetadataResolver
    @Inject
    constructor(
        @RestClient
        private val service: TmdbService,
        private val converter: TmdbResponseConverter,
        private val logger: Logger,
    ) : MovieMetadataResolver {
        override fun resolveByUrl(url: String): MovieMetadata {
            val response =
                try {
                    service.resolveMetadata(TmdbUrlRequest(url))
                } catch (e: ClientWebApplicationException) {
                    if (e.response.status == 404) {
                        throw MovieNotFoundException("Could not find movie: ${e.response.readEntity(String::class.java)}")
                    }

                    throw IOException(e)
                }
            return converter.toModel(response)
        }

        override fun resolveById(
            id: String,
            idSource: MetadataSourceType?,
        ): MovieMetadata {
            val externalSource =
                when (idSource) {
                    null -> null
                    MetadataSourceType.TMDB -> null
                    MetadataSourceType.IMDB -> "imdb"
                }

            logger.info("Resolving TMDB metadata for $id (external source: $externalSource)")

            val response =
                try {
                    service.resolveMetadataById(id, externalSource = externalSource)
                } catch (e: ClientWebApplicationException) {
                    if (e.response.status == 404) {
                        throw MovieNotFoundException("Movie not found on TMDB")
                    }

                    throw IOException(e)
                }
            return converter.toModel(response)
        }
    }
