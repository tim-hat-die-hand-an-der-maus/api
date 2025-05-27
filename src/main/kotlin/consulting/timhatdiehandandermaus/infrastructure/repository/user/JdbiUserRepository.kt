package consulting.timhatdiehandandermaus.infrastructure.repository.user

import consulting.timhatdiehandandermaus.application.exception.DuplicateException
import consulting.timhatdiehandandermaus.application.exception.NotFoundException
import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.model.TelegramUser
import consulting.timhatdiehandandermaus.application.repository.UserRepository
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.useTransactionUnchecked
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.kotlin.BindKotlin
import org.jdbi.v3.sqlobject.kotlin.attach
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.mapstruct.Mapper
import org.postgresql.util.PSQLException
import java.io.IOException
import java.util.UUID

@RequestScoped
class JdbiUserRepository
    @Inject
    constructor(
        private val jdbi: Jdbi,
        private val mapper: UserDaoMapper,
    ) : UserRepository {
        override fun getById(id: UUID): CanonicalUser? =
            jdbi.withHandleUnchecked { handle ->
                handle.attach<UserDao>().findById(id)?.let(mapper::toModel)
            }

        override fun getByTelegramId(id: Long): CanonicalUser? =
            jdbi.withHandleUnchecked { handle ->
                handle.attach<UserDao>().findByTelegramId(id)?.let(mapper::toModel)
            }

        override fun insert(user: CanonicalUser) {
            jdbi.useTransactionUnchecked { handle ->
                val dao = handle.attach<UserDao>()
                try {
                    dao.insert(mapper.toRow(user))
                } catch (e: UnableToExecuteStatementException) {
                    val cause = e.cause
                    if (cause is PSQLException) {
                        when (cause.sqlState) {
                            "23505" -> throw DuplicateException(user.id)
                        }
                    }
                    throw IOException(e)
                }
            }
        }

        override fun insertTelegramUser(
            canonicalId: UUID,
            telegramUser: TelegramUser,
        ) {
            jdbi.useTransactionUnchecked { handle ->
                val dao = handle.attach<UserDao>()
                try {
                    dao.insert(canonicalId, mapper.toRow(telegramUser))
                } catch (e: UnableToExecuteStatementException) {
                    val cause = e.cause
                    if (cause is PSQLException) {
                        when (cause.sqlState) {
                            "23503" -> throw NotFoundException("User not found: $canonicalId")
                            "23505" -> throw DuplicateException(canonicalId)
                        }
                    }
                    throw IOException(e)
                }
            }
        }

        @Throws(NotFoundException::class)
        override fun updateCanonicalUser(user: CanonicalUser) {
            jdbi.useTransactionUnchecked { handle ->
                val dao = handle.attach<UserDao>()
                if (!dao.update(mapper.toRow(user))) {
                    throw NotFoundException("User not found: $user")
                }
            }
        }

        @Throws(NotFoundException::class)
        override fun updateTelegramUser(telegramUser: TelegramUser) {
            jdbi.useTransactionUnchecked { handle ->
                val dao = handle.attach<UserDao>()
                if (!dao.update(mapper.toRow(telegramUser))) {
                    throw NotFoundException("Telegram user not found: $telegramUser")
                }
            }
        }
    }

private interface UserDao {
    @SqlQuery("SELECT * FROM canonical_user where id = :id")
    fun findById(id: UUID): CanonicalUserRow?

    @SqlQuery("SELECT c.id, c.display_name FROM canonical_user c JOIN telegram_user u ON c.id = u.canonical_id WHERE u.id = :id")
    fun findByTelegramId(id: Long): CanonicalUserRow?

    @SqlUpdate("insert into canonical_user (id, display_name) values (:id, :displayName)")
    fun insert(
        @BindKotlin user: CanonicalUserRow,
    )

    @SqlUpdate(
        """
        insert into telegram_user (id, canonical_id, first_name, last_name)
        values (:id, :canonicalId, :firstName, :lastName)
        """,
    )
    fun insert(
        canonicalId: UUID,
        @BindKotlin user: TelegramUserRow,
    )

    @SqlUpdate(
        """
            update telegram_user
            set first_name = :firstName, last_name = :lastName
            where id = :id
        """,
    )
    fun update(
        @BindKotlin telegramUserRow: TelegramUserRow,
    ): Boolean

    @SqlUpdate("update canonical_user set display_name = :displayName where id = :id")
    fun update(
        @BindKotlin canonicalUserRow: CanonicalUserRow,
    ): Boolean
}

@Mapper
interface UserDaoMapper {
    fun toModel(canonicalUserRow: CanonicalUserRow): CanonicalUser

    fun toRow(canonicalUser: CanonicalUser): CanonicalUserRow

    fun toRow(telegramUser: TelegramUser): TelegramUserRow
}

@RegisterForReflection
data class CanonicalUserRow(
    var id: UUID,
    var displayName: String,
)

@RegisterForReflection
data class TelegramUserRow(
    var id: Long,
    var firstName: String,
    var lastName: String?,
)
