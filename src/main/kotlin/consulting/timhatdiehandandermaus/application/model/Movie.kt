package consulting.timhatdiehandandermaus.application.model

import java.time.Instant
import java.util.UUID

class Movie(
    val id: UUID,
    var status: MovieStatus,
    var metadata: MovieMetadata,
    var metadataUpdateTime: Instant,
)
