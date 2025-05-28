package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.repository.UserRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import java.util.UUID

@RequestScoped
class GetUserById
    @Inject
    constructor(
        private val repo: UserRepository,
    ) {
        operator fun invoke(userId: UUID): CanonicalUser? = repo.getById(userId)
    }
