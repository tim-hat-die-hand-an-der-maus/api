package consulting.timhatdiehandandermaus.infrastructure.repository.queue

import com.radcortez.flyway.test.annotation.DataSource
import com.radcortez.flyway.test.annotation.FlywayTest
import consulting.timhatdiehandandermaus.application.exception.DuplicateException
import consulting.timhatdiehandandermaus.application.exception.NotFoundException
import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.model.TelegramUser
import consulting.timhatdiehandandermaus.application.repository.UserRepository
import consulting.timhatdiehandandermaus.domain.model.DummyDataResolver
import consulting.timhatdiehandandermaus.infrastructure.repository.QuarkusDataSourceProvider
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@QuarkusTest
@FlywayTest(DataSource(QuarkusDataSourceProvider::class))
@ExtendWith(DummyDataResolver::class)
class UserRepositoryTest {
    @Inject
    lateinit var repo: UserRepository

    @Test
    fun testInsertCanonicalUser(user: CanonicalUser) {
        assertDoesNotThrow {
            repo.insert(user)
        }

        assertEquals(user, repo.getById(user.id))
    }

    @Test
    fun testInsertCanonicalUserDuplicate(user: CanonicalUser) {
        repo.insert(user)
        assertThrows<DuplicateException> {
            repo.insert(user)
        }
    }

    @Test
    fun testInsertTelegramUser(
        canonicalUser: CanonicalUser,
        user: TelegramUser,
    ) {
        assertDoesNotThrow {
            repo.insert(canonicalUser)
            repo.insertTelegramUser(canonicalUser.id, user)
        }

        val associatedUser = repo.getByTelegramId(user.id)
        assertEquals(canonicalUser, associatedUser)
    }

    @Test
    fun testInsertTelegramUserDuplicate(
        canonicalUser: CanonicalUser,
        user: TelegramUser,
    ) {
        repo.insert(canonicalUser)
        repo.insertTelegramUser(canonicalUser.id, user)
        assertThrows<DuplicateException> {
            repo.insertTelegramUser(canonicalUser.id, user)
        }
    }

    @Test
    fun testInsertSecondTelegramUserForCanonical(
        canonicalUser: CanonicalUser,
        user1: TelegramUser,
        user2: TelegramUser,
    ) {
        repo.insert(canonicalUser)
        repo.insertTelegramUser(canonicalUser.id, user1)
        assertThrows<DuplicateException> {
            repo.insertTelegramUser(canonicalUser.id, user2)
        }
    }

    @Test
    fun testInsertTelegramUserMultiCanonical(
        canonicalUser: CanonicalUser,
        user: TelegramUser,
    ) {
        repo.insert(canonicalUser)
        repo.insertTelegramUser(canonicalUser.id, user)
        assertThrows<DuplicateException> {
            repo.insertTelegramUser(UUID.randomUUID(), user)
        }
    }

    @Test
    fun testInsertTelegramUserNotFound(user: TelegramUser) {
        assertThrows<NotFoundException> {
            repo.insertTelegramUser(UUID.randomUUID(), user)
        }
    }

    @Test
    fun updateUnknownTelegramUser(user: TelegramUser) {
        assertThrows<NotFoundException> {
            repo.updateTelegramUser(user)
        }
    }

    @Test
    fun updateUnknownUser(user: CanonicalUser) {
        assertThrows<NotFoundException> {
            repo.updateCanonicalUser(user)
        }
    }

    @Test
    fun updateUser(user: CanonicalUser) {
        repo.insert(user)
        val updated = user.copy(displayName = "new name")
        repo.updateCanonicalUser(updated)
        assertEquals(updated, repo.getById(user.id))
    }

    @Test
    fun updateTelegramUser(
        canonicalUser: CanonicalUser,
        user: TelegramUser,
    ) {
        repo.insert(canonicalUser)
        repo.insertTelegramUser(canonicalUser.id, user)
        val updated =
            user.copy(
                firstName = "new first name",
                lastName = "new last name",
            )

        assertDoesNotThrow {
            repo.updateTelegramUser(updated)
        }
    }
}
