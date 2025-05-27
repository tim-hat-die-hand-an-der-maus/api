package consulting.timhatdiehandandermaus.iface.api.model

import consulting.timhatdiehandandermaus.application.model.TelegramUser
import org.mapstruct.Mapper

data class TelegramUserRequest(
    val id: Long,
    val firstName: String,
    val lastName: String?,
)

@Mapper
interface UserRequestMapper {
    fun toModel(telegramUserRequest: TelegramUserRequest): TelegramUser
}
