// ktlint-disable filename

package consulting.timhatdiehandandermaus.iface.api.model

import com.fasterxml.jackson.annotation.JsonCreator
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import org.mapstruct.Mapper
import org.mapstruct.ValueMapping

data class MoviePostRequest @JsonCreator constructor(val imdbUrl: String)

enum class MovieDeleteStatus {
    Deleted,
    Watched,
}

@Suppress("EnumEntryName")
enum class MovieMetadataField {
    cover,
    coverUrl,
    rating,
    ;
}

data class MovieMetadataPatchRequest @JsonCreator constructor(
    val refresh: List<MovieMetadataField>,
)

@Mapper
interface MovieRequestConverter {
    fun toMovieStatus(movieDeleteStatus: MovieDeleteStatus): MovieStatus
}

private typealias DomainMovieMetadataField = consulting.timhatdiehandandermaus.domain.value.MovieMetadataField

@Mapper
interface MovieMetadataFieldConverter {
    @ValueMapping(source = "cover", target = "Cover")
    @ValueMapping(source = "coverUrl", target = "CoverUrl")
    @ValueMapping(source = "rating", target = "Rating")
    fun toDomain(
        movieMetadataField: MovieMetadataField,
    ): DomainMovieMetadataField
}
