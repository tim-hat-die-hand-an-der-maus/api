package consulting.timhatdiehandandermaus.domain.model

data class MovieMetadata(
    val id: String,
    val title: String,
    val year: Int,
    val rating: String,
    val coverUrl: String?,
)
