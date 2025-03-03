

@file:Suppress("ktlint:standard:filename")

package consulting.timhatdiehandandermaus.iface.api.model

import com.fasterxml.jackson.annotation.JsonCreator
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import org.mapstruct.Mapper
import org.mapstruct.ValueMapping

data class MoviePostRequest
    @JsonCreator
    constructor(
        val imdbUrl: String,
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

@Suppress("EnumEntryName", "ktlint:standard:enum-entry-name-case")
enum class MovieMetadataField {
    cover,
    rating,
    title,
}

data class MovieMetadataPatchRequest
    @JsonCreator
    constructor(
        val refresh: List<MovieMetadataField>,
    )

@Mapper
interface MovieRequestConverter {
    fun toMovieStatus(movieDeleteStatus: MovieDeleteStatus): MovieStatus

    fun toMovieStatus(movieGetStatus: MovieGetStatus): MovieStatus
}

private typealias DomainMovieMetadataField = consulting.timhatdiehandandermaus.application.model.MovieMetadataField

@Mapper
interface MovieMetadataFieldConverter {
    @ValueMapping(source = "cover", target = "Cover")
    @ValueMapping(source = "rating", target = "Rating")
    @ValueMapping(source = "title", target = "Title")
    fun toDomain(movieMetadataField: MovieMetadataField): DomainMovieMetadataField
}
