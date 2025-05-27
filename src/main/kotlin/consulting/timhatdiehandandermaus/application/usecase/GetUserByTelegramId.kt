package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.repository.UserRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject

@RequestScoped
class GetUserByTelegramId
    @Inject
    constructor(
        private val repo: UserRepository,
    ) {
        operator fun invoke(telegramUserId: Long): CanonicalUser? = repo.getByTelegramId(telegramUserId)
    }
