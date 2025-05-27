package consulting.timhatdiehandandermaus.application.model

import java.util.UUID

data class CanonicalUser(
    val id: UUID,
    val displayName: String,
)

data class TelegramUser(
    val id: Long,
    val firstName: String,
    val lastName: String?,
) {
    fun deriveDisplayName(): String = if (lastName == null) firstName else "$firstName $lastName"
}
