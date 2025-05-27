package consulting.timhatdiehandandermaus.iface.api.model

import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import org.mapstruct.Mapper
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val displayName: String,
)

@Mapper
interface UserResponseMapper {
    fun toDto(user: CanonicalUser): UserResponse
}
