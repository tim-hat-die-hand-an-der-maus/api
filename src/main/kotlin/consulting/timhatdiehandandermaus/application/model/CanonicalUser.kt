package consulting.timhatdiehandandermaus.application.model

import java.util.UUID

data class TelegramUser(
    val id: Long,
    val firstName: String,
    val lastName: String?,
) {
    fun generateDisplayName(): String {
        if (lastName == null) {
            return firstName
        }

        return "$firstName $lastName"
    }
}

data class CanonicalUser(
    val id: UUID,
    var displayName: String,
    val telegram: TelegramUser,
)
