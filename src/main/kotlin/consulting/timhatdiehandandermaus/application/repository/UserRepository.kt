package consulting.timhatdiehandandermaus.application.repository

import consulting.timhatdiehandandermaus.application.exception.DuplicateException
import consulting.timhatdiehandandermaus.application.exception.NotFoundException
import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.model.TelegramUser
import java.util.UUID

interface UserRepository {
    fun getByTelegramId(id: Long): CanonicalUser?

    fun getById(id: UUID): CanonicalUser?

    fun insert(user: CanonicalUser)

    @Throws(NotFoundException::class, DuplicateException::class)
    fun insertTelegramUser(
        canonicalId: UUID,
        telegramUser: TelegramUser,
    )

    @Throws(NotFoundException::class)
    fun updateTelegramUser(telegramUser: TelegramUser)

    @Throws(NotFoundException::class)
    fun updateCanonicalUser(user: CanonicalUser)
}
