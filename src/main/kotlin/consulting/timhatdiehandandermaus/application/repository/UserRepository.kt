package consulting.timhatdiehandandermaus.application.repository

import consulting.timhatdiehandandermaus.application.exception.DuplicateException
import consulting.timhatdiehandandermaus.application.exception.NotFoundException
import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.model.TelegramUser
import java.util.UUID

interface UserRepository {
    @Throws(DuplicateException::class)
    fun insert(user: CanonicalUser)

    fun findByTelegramId(id: Long): CanonicalUser?

    fun findById(id: UUID): CanonicalUser?

    @Throws(NotFoundException::class)
    fun updateMetadata(
        id: UUID,
        displayName: String,
    )

    @Throws(NotFoundException::class)
    fun updateTelegramInfo(telegramUser: TelegramUser)
}
