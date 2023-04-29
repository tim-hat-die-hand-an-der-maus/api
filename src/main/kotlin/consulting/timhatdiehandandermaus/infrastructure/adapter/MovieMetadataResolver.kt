package consulting.timhatdiehandandermaus.infrastructure.adapter

import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.mapstruct.Mapper

data class ImdbRequest(val imdbUrl: String)
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
)

@RegisterRestClient(configKey = "imdb-api")
interface ImdbService {
    @POST
    fun resolveMetadata(request: ImdbRequest): ImdbResponse
}

@Mapper
interface ResponseConverter {
    fun toModel(response: ImdbResponse): MovieMetadata
}

@RequestScoped
class ImdbMovieMetadataResolver @Inject constructor(
    @RestClient
    private val service: ImdbService,
    private val converter: ResponseConverter,
) : MovieMetadataResolver {

    override fun resolveImdb(imdbUrl: String): MovieMetadata {
        val response = service.resolveMetadata(ImdbRequest(imdbUrl))
        return converter.toModel(response)
    }

    override fun resolveImdbById(imdbId: String): MovieMetadata {
        val imdbUrl = "https://imdb.com/tt$imdbId/"
        return resolveImdb(imdbUrl)
    }
}
