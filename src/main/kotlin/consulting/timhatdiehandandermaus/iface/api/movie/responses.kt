package consulting.timhatdiehandandermaus.iface.api.movie

enum class MovieStatusResponse {
    Queued,
    Watched,
    Deleted,
}

data class MovieResponse(
    val id: String,
    val status: MovieStatusResponse,
    val imdb: MovieMetadataResponse,
)

data class MovieMetadataResponse(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
)
