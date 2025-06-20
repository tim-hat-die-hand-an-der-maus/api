package consulting.timhatdiehandandermaus.infrastructure.adapter

import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.port.MetadataSource
import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.io.IOException

data class ImdbRequest(
    val imdbUrl: String,
)

data class CoverResponse(
    val url: String,
    val ratio: Double,
)

data class ImdbResponse(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
    val cover: CoverResponse,
    val imdbUrl: String,
)

@RegisterRestClient(configKey = "imdb-api")
interface ImdbService {
    @POST
    fun resolveMetadata(request: ImdbRequest): ImdbResponse
}

@Mapper
interface ResponseConverter {
    @Mapping(target = "infoPageUrl", source = "imdbUrl")
    @Mapping(target = "type", expression = "java(consulting.timhatdiehandandermaus.application.model.MetadataSourceType.IMDB)")
    @Mapping(target = "updateTime", expression = "java(java.time.Instant.now())")
    fun toModel(response: ImdbResponse): MovieMetadata
}

@MetadataSource(MetadataSourceType.IMDB)
@RequestScoped
class ImdbMovieMetadataResolver
    @Inject
    constructor(
        @RestClient
        private val service: ImdbService,
        private val converter: ResponseConverter,
    ) : MovieMetadataResolver {
        override fun resolveByUrl(url: String): MovieMetadata {
            val response =
                try {
                    service.resolveMetadata(ImdbRequest(url))
                } catch (e: ClientWebApplicationException) {
                    if (e.response.status == 404) {
                        throw MovieNotFoundException("Could not find movie on IMDb: $url")
                    } else {
                        throw IOException(e)
                    }
                }
            return converter.toModel(response)
        }

        override fun resolveById(
            id: String,
            idSource: MetadataSourceType?,
        ): MovieMetadata {
            if (idSource != null && idSource != MetadataSourceType.IMDB) {
                throw MovieNotFoundException("Can not look up foreign IDs on IMDb")
            }
            val imdbUrl = "https://imdb.com/tt$id/"
            return resolveByUrl(imdbUrl)
        }
    }
