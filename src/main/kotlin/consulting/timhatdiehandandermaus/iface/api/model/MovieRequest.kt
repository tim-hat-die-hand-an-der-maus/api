@file:Suppress("ktlint:standard:filename")

package consulting.timhatdiehandandermaus.iface.api.model

import com.fasterxml.jackson.annotation.JsonCreator
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import org.mapstruct.Mapper
import java.util.UUID

data class MoviePostRequest
    @JsonCreator
    constructor(
        val imdbUrl: String,
        val userId: UUID? = null,
    )

enum class MovieDeleteStatus {
    Deleted,
    Watched,
}

enum class MovieGetStatus {
    Queued,
    Deleted,
    Watched,
}

@Mapper
interface MovieRequestConverter {
    fun toMovieStatus(movieDeleteStatus: MovieDeleteStatus): MovieStatus

    fun toMovieStatus(movieGetStatus: MovieGetStatus): MovieStatus
}
