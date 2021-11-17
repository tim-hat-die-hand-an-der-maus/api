package consulting.timhatdiehandandermaus.application.port

import consulting.timhatdiehandandermaus.domain.model.MovieMetadata

interface MovieMetadataResolver {
    fun resolveImdb(imdbUrl: String): MovieMetadata
}
