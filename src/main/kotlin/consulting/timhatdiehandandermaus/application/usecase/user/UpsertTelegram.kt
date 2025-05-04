package consulting.timhatdiehandandermaus.application.usecase.user

import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.model.TelegramUser
import consulting.timhatdiehandandermaus.application.repository.UserRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import java.util.UUID

@RequestScoped
class UpsertTelegram
    @Inject
    constructor(
        private val userRepo: UserRepository,
    ) {
        operator fun invoke(telegramUser: TelegramUser): CanonicalUser {
            val user = userRepo.findByTelegramId(telegramUser.id)

            if (user == null) {
                val newUser =
                    CanonicalUser(
                        UUID.randomUUID(),
                        telegramUser.generateDisplayName(),
                        telegramUser,
                    )
                userRepo.insert(newUser)
                return newUser
            }

            userRepo.updateMetadata(user.id, telegramUser.generateDisplayName())
            userRepo.updateTelegramInfo(telegramUser)
            return user
        }
    }
