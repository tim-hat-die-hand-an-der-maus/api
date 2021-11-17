package consulting.timhatdiehandandermaus.infrastructure.adapter

import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.mapstruct.Mapper
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.POST

private data class ImdbRequest(val imdbUrl: String)
private data class ImdbResponse(
    val id: String,
    val title: String,
    val year: Int,
    val rating: Float,
)

@RegisterRestClient(configKey = "imdb-api")
private interface ImdbService {
    @POST
    fun resolveMetadata(request: ImdbRequest): ImdbResponse
}

@Mapper
private interface ResponseConverter {
    fun toModel(response: ImdbResponse): MovieMetadata
}

@RequestScoped
class ImdbMovieMetadataResolver @Inject private constructor(
    @RestClient
    private val service: ImdbService,
    private val converter: ResponseConverter,
) : MovieMetadataResolver {

    override fun resolveImdb(imdbUrl: String): MovieMetadata {
        val response = service.resolveMetadata(ImdbRequest(imdbUrl))
        return converter.toModel(response)
    }
}
