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
import org.jdbi.v3.core.kotlin.inTransactionUnchecked
import org.jdbi.v3.core.kotlin.useTransactionUnchecked
import org.jdbi.v3.core.mapper.JoinRow
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.config.RegisterJoinRowMapper
import org.jdbi.v3.sqlobject.kotlin.BindKotlin
import org.jdbi.v3.sqlobject.kotlin.attach
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.mapstruct.Mapper
import org.mapstruct.Mapping
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
        override fun insert(user: CanonicalUser) {
            try {
                jdbi.useTransactionUnchecked { handle ->
                    handle.attach<CanonicalUserDao>().insert(user.id, user.displayName)
                    handle.attach<TelegramUserDao>().insert(user.id, user.telegram)
                }
            } catch (e: UnableToExecuteStatementException) {
                val cause = e.cause
                if (cause is PSQLException && cause.sqlState == "23505") {
                    throw DuplicateException(user.id)
                }
                throw IOException(e)
            }
        }

        override fun findByTelegramId(id: Long): CanonicalUser? =
            jdbi.inTransactionUnchecked { handle ->
                handle.attach<CanonicalUserDao>().findByTelegramId(id)?.let(mapper::joinedUser)
            }

        override fun findById(id: UUID): CanonicalUser? =
            jdbi.inTransactionUnchecked { handle ->
                handle.attach<CanonicalUserDao>().findById(id)?.let(mapper::joinedUser)
            }

        override fun updateMetadata(
            id: UUID,
            displayName: String,
        ) = jdbi.useTransactionUnchecked { handle ->
            val isUpdated = handle.attach<CanonicalUserDao>().updateMetadata(id, displayName)
            if (!isUpdated) {
                throw NotFoundException()
            }
        }

        override fun updateTelegramInfo(telegramUser: TelegramUser) =
            jdbi.useTransactionUnchecked { handle ->
                val isUpdated = handle.attach<TelegramUserDao>().update(telegramUser)
                if (!isUpdated) {
                    throw NotFoundException()
                }
            }
    }

private interface CanonicalUserDao {
    @SqlUpdate(
        """
            insert into canonical_user (id, display_name) values (:id, :displayName)
        """,
    )
    fun insert(
        id: UUID,
        displayName: String,
    )

    @SqlUpdate(
        """
            update canonical_user
            set
              display_name = :displayName
            where id = :id
        """,
    )
    fun updateMetadata(
        id: UUID,
        displayName: String,
    ): Boolean

    @SqlQuery(
        """
            select
                c.id as c_id,
                c.display_name as c_display_name,
                t.id as t_id,
                t.first_name as t_first_name,
                t.last_name as t_last_name
            from canonical_user c
            join telegram_user t on t.canonical_id = c.id
            where c.id = :id
        """,
    )
    @RegisterJoinRowMapper(CanonicalUser::class, TelegramUser::class)
    fun findById(id: UUID): JoinRow?

    @SqlQuery(
        """
            select
                c.id as c_id,
                c.display_name as c_display_name,
                t.id as t_id,
                t.first_name as t_first_name,
                t.last_name as t_last_name
            from canonical_user c
            join telegram_user t on t.canonical_id = c.id
            where t.id = :id
        """,
    )
    @RegisterJoinRowMapper(CanonicalUser::class, TelegramUser::class)
    fun findByTelegramId(id: Long): JoinRow?
}

private interface TelegramUserDao {
    @SqlUpdate(
        """
            insert into telegram_user (canonical_id, id, first_name, last_name) values (
                :canonicalId,
                :user.id,
                :user.firstName,
                :user.lastName
            )
        """,
    )
    fun insert(
        canonicalId: UUID,
        @BindKotlin("user") user: TelegramUser,
    )

    @SqlUpdate(
        """
            update telegram_user set
                first_name = :firstName,
                last_name = :lastName
            where id = :user.id
        """,
    )
    fun update(
        @BindKotlin user: TelegramUser,
    ): Boolean
}

@RegisterForReflection
data class CanonicalUserRow(
    val id: UUID,
    val displayName: String,
)

@RegisterForReflection
data class TelegramUserRow(
    val id: Long,
    val firstName: String,
    val lastName: String?,
)

@Mapper
interface UserDaoMapper {
    @Mapping(target = "id", source = "canonicalUserRow.id")
    @Mapping(target = "telegram", source = "telegramUser")
    fun toModel(
        canonicalUserRow: CanonicalUserRow,
        telegramUser: TelegramUser,
    ): CanonicalUser

    fun toModel(telegramUser: TelegramUserRow): TelegramUser

    fun joinedUser(joinRow: JoinRow): CanonicalUser {
        val telegram = joinRow.get(TelegramUserRow::class.java)
        val canonical = joinRow.get(CanonicalUserRow::class.java)
        return toModel(canonical, toModel(telegram))
    }
}
