package consulting.timhatdiehandandermaus.application.port

import consulting.timhatdiehandandermaus.application.model.MovieMetadata

interface MovieMetadataResolver {
    fun resolveImdb(imdbUrl: String): MovieMetadata

    fun resolveImdbById(imdbId: String): MovieMetadata
}
