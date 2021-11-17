// ktlint-disable filename

package consulting.timhatdiehandandermaus.iface.api.movie

import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.iface.api.mapper.UuidMapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(uses = [UuidMapper::class])
interface MovieConverter {

    @Mapping(source = "metadata", target = "imdb")
    fun convertToResponse(movie: Movie): MovieResponse
}
