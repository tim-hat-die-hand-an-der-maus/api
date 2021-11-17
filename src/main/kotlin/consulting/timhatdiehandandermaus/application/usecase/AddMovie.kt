package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class AddMovie @Inject constructor(
    private val metadataResolver: MovieMetadataResolver,
) {

    operator fun invoke(imdbUrl: String): Movie {
        // TODO error handling
        val metadata = metadataResolver.resolveImdb(imdbUrl)
        // TODO: persist
        // TODO: check duplicates

        return Movie(
            id = UUID.randomUUID(),
            status = MovieStatus.Queued,
            metadata = metadata,
        )
    }
}
