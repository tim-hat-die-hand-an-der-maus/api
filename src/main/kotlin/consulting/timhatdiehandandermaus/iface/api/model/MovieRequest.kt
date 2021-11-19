// ktlint-disable filename

package consulting.timhatdiehandandermaus.iface.api.model

import com.fasterxml.jackson.annotation.JsonCreator
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import org.mapstruct.Mapper

data class MoviePostRequest @JsonCreator constructor(val imdbUrl: String)

enum class MovieDeleteStatus {
    Deleted,
    Watched,
}

@Mapper
interface MovieRequestConverter {
    fun toMovieStatus(movieDeleteStatus: MovieDeleteStatus): MovieStatus
}
