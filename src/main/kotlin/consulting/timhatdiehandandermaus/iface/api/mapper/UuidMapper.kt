package consulting.timhatdiehandandermaus.iface.api.mapper

import org.mapstruct.Mapper
import java.util.UUID

@Mapper
interface UuidMapper {
    fun toDto(uuid: UUID): String = uuid.toString()
}
