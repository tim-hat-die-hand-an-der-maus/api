package consulting.timhatdiehandandermaus.domain.model

data class MovieMetadata(
    /** IMDb ID */
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
    val coverUrl: String,
)
