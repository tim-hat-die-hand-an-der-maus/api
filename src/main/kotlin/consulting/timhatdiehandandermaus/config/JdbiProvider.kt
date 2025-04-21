package consulting.timhatdiehandandermaus.config

import io.agroal.api.AgroalDataSource
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.enums.EnumStrategy
import org.jdbi.v3.core.enums.Enums
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin

class JdbiProvider
    @Inject
    constructor(
        private val dataSource: AgroalDataSource,
    ) {
        @Singleton
        @Produces
        fun provideJdbi(): Jdbi =
            Jdbi.create(dataSource).apply {
                installPlugin(PostgresPlugin())
                installPlugin(SqlObjectPlugin())
                installPlugin(KotlinPlugin())
                installPlugin(KotlinSqlObjectPlugin())
                configure(Enums::class.java) { it.setEnumStrategy(EnumStrategy.BY_NAME) }
            }
    }
