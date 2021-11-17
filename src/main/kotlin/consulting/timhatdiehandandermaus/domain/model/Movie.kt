package consulting.timhatdiehandandermaus.domain.model

import java.util.UUID

class Movie(
    val id: UUID,
    var status: MovieStatus,
    var metadata: MovieMetadata,
)
