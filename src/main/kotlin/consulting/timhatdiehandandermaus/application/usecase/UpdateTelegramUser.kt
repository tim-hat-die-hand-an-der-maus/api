package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.exception.DuplicateException
import consulting.timhatdiehandandermaus.application.exception.NotFoundException
import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.model.TelegramUser
import consulting.timhatdiehandandermaus.application.repository.UserRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import java.util.UUID

@RequestScoped
class UpdateTelegramUser
    @Inject
    constructor(
        private val repo: UserRepository,
    ) {
        operator fun invoke(
            telegramUser: TelegramUser,
            existingUserId: UUID?,
        ): CanonicalUser {
            // This use case desperately needs transaction control

            val existingAssociatedUser = repo.getByTelegramId(telegramUser.id)
            if (existingAssociatedUser != null && existingUserId != null && existingAssociatedUser.id != existingUserId) {
                throw DuplicateException(existingUserId)
            }

            val canonicalUser: CanonicalUser
            if (existingAssociatedUser == null && existingUserId != null) {
                canonicalUser =
                    repo.getById(existingUserId)?.copy(displayName = telegramUser.deriveDisplayName())
                        ?: throw NotFoundException("User not found")
                repo.updateCanonicalUser(canonicalUser)
                repo.insertTelegramUser(canonicalUser.id, telegramUser)
            } else if (existingAssociatedUser == null) {
                canonicalUser =
                    CanonicalUser(
                        id = UUID.randomUUID(),
                        displayName = telegramUser.deriveDisplayName(),
                    )
                repo.insert(canonicalUser)
                repo.insertTelegramUser(canonicalUser.id, telegramUser)
            } else {
                canonicalUser = existingAssociatedUser.copy(displayName = telegramUser.deriveDisplayName())
                repo.updateCanonicalUser(canonicalUser)
                repo.updateTelegramUser(telegramUser)
            }

            return canonicalUser
        }
    }
