package consulting.timhatdiehandandermaus.infrastructure.repository.movie

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.CoverMetadata
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import io.quarkus.narayana.jta.runtime.TransactionConfiguration
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.inTransactionUnchecked
import org.jdbi.v3.core.kotlin.useHandleUnchecked
import org.jdbi.v3.core.kotlin.useTransactionUnchecked
import org.jdbi.v3.core.mapper.JoinRow
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.config.RegisterJoinRowMapper
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.attach
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.postgresql.util.PSQLException
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import java.util.stream.Stream
import kotlin.streams.asSequence

@RequestScoped
class JdbiMovieRepository
    @Inject
    constructor(
        private val jdbi: Jdbi,
        private val mapper: MovieDaoMapper,
    ) : MovieRepository {
        @Throws(DuplicateMovieException::class)
        override fun insert(movie: MovieInsertDto): UUID =
            jdbi.inTransactionUnchecked { handle ->
                val existing =
                    handle
                        .createQuery(
                            """
                            select movie_id from metadata
                            where (metadata.source_id, metadata.source_type) in (<metadata>)
                            """.trimIndent(),
                        ).bindBeanList("metadata", movie.availableMetadata, listOf("id", "type"))
                        .map { it.getColumn("movie_id", UUID::class.java) }
                        .findFirst()

                if (existing.isPresent) {
                    throw DuplicateMovieException(existing.get())
                }

                val movieId = UUID.randomUUID()
                handle.attach<MovieDao>().insertMovie(movieId, movie.status)
                handle.attach<MetadataDao>().insertBatch(movieId, movie.availableMetadata)

                val coverDao = handle.attach<CoverDao>()
                for (meta in movie.availableMetadata) {
                    val cover = meta.cover ?: continue
                    coverDao.upsertCover(meta.id, meta.type, cover)
                }

                movieId
            }

        @Throws(MovieNotFoundException::class)
        override fun updateMetadata(
            id: UUID,
            metadata: MovieMetadata,
        ) {
            jdbi.useTransactionUnchecked { handle ->
                try {
                    handle.attach<MetadataDao>().upsert(id, metadata)
                } catch (e: UnableToExecuteStatementException) {
                    val cause = e.cause
                    if (cause is PSQLException && cause.sqlState == "23505") {
                        throw DuplicateMovieException(id)
                    }
                    throw e
                }
                metadata.cover?.let {
                    handle
                        .attach<CoverDao>()
                        .upsertCover(metadata.id, metadata.type, it)
                }
            }
        }

        @Throws(MovieNotFoundException::class)
        override fun updateStatus(
            id: UUID,
            status: MovieStatus,
        ) {
            jdbi.useHandleUnchecked { handle ->
                if (!handle.attach<MovieDao>().updateStatus(id, status)) {
                    throw MovieNotFoundException()
                }
            }
        }

        override fun find(id: UUID): Movie? {
            return jdbi.inTransactionUnchecked { handle ->
                val movieRow = handle.attach<MovieDao>().findById(id)
                if (movieRow == null) {
                    return@inTransactionUnchecked null
                }

                val metadata = handle.attach<MetadataDao>().findForMovie(id).map(mapper::joinToMetadata)
                return@inTransactionUnchecked mapper.toModel(movieRow, metadata)
            }
        }

        @TransactionConfiguration(timeout = 3600)
        override fun forEachMovie(
            metadataUpdateTimeCutoff: Instant?,
            limit: Long,
            action: (Movie) -> Unit,
        ) {
            jdbi.useTransactionUnchecked { handle ->
                val movieDao = handle.attach<MovieDao>()
                var movieRowStream =
                    if (metadataUpdateTimeCutoff == null) {
                        movieDao.streamAll()
                    } else {
                        movieDao.streamOutdated(
                            metadataUpdateTimeCutoff.atZone(ZoneOffset.UTC),
                        )
                    }

                if (limit > 0) {
                    movieRowStream = movieRowStream.limit(limit)
                }

                val metadataDao = handle.attach<MetadataDao>()

                val movieStream =
                    movieRowStream.map { movieRow ->
                        val metadata = metadataDao.findForMovie(movieRow.id).map(mapper::joinToMetadata)
                        mapper.toModel(movieRow, metadata)
                    }

                movieStream.forEach(action)
            }
        }

        override fun listMovies(status: MovieStatus?): Sequence<Movie> =
            jdbi.inTransactionUnchecked { handle ->
                val movieDao = handle.attach<MovieDao>()
                val movieRows =
                    if (status == null) {
                        movieDao.streamAll().asSequence()
                    } else {
                        movieDao.streamAllByStatus(status).asSequence()
                    }

                val metadataDao = handle.attach<MetadataDao>()

                movieRows.map { movieRow ->
                    val metadata = metadataDao.findForMovie(movieRow.id).map(mapper::joinToMetadata)
                    mapper.toModel(movieRow, metadata)
                }
            }
    }

private interface MovieDao {
    @SqlUpdate("insert into movie (id, status) values (:id, :status)")
    fun insertMovie(
        id: UUID,
        status: MovieStatus,
    )

    @SqlUpdate("update movie set status = :status where id = :id")
    fun updateStatus(
        id: UUID,
        status: MovieStatus,
    ): Boolean

    @SqlQuery("select * from movie where id = :id")
    fun findById(id: UUID): MovieRow?

    @SqlQuery("select * from movie where status = :status")
    fun streamAllByStatus(status: MovieStatus): Stream<MovieRow>

    @SqlQuery(
        """
            select distinct(m.id), m.status from movie m
            join metadata meta on m.id = meta.movie_id
            where meta.update_time < :updateTimeCutoff
        """,
    )
    fun streamOutdated(updateTimeCutoff: ZonedDateTime): Stream<MovieRow>

    @SqlQuery("select * from movie")
    fun streamAll(): Stream<MovieRow>
}

private interface MetadataDao {
    @SqlBatch(
        """
            insert into metadata(
                movie_id,
                source_id,
                source_type,
                title,
                year,
                rating,
                info_page_url,
                update_time
            ) values (
                :movieId,
                :id,
                :type,
                :title,
                :year,
                :rating,
                :infoPageUrl,
                :updateTime
            )
        """,
    )
    fun insertBatch(
        movieId: UUID,
        @BindBean metadata: List<MovieMetadata>,
    )

    @SqlUpdate(
        """
            insert into metadata(
                movie_id,
                source_id,
                source_type,
                title,
                year,
                rating,
                info_page_url,
                update_time
            ) values (
                :movieId,
                :id,
                :type,
                :title,
                :year,
                :rating,
                :infoPageUrl,
                :updateTime
            ) on conflict (source_id, source_type) do update set
                title = excluded.title,
                year = excluded.year,
                rating = excluded.rating,
                info_page_url = excluded.info_page_url,
                update_time = excluded.update_time
        """,
    )
    fun upsert(
        movieId: UUID,
        @BindBean metadata: MovieMetadata,
    )

    @SqlQuery(
        """
            select * from metadata m
            left join cover c on m.source_id = c.metadata_source_id and m.source_type = c.metadata_source_type
            where m.movie_id = :movieId
        """,
    )
    @RegisterJoinRowMapper(MetadataRow::class, CoverRow::class)
    fun findForMovie(movieId: UUID): List<JoinRow>
}

private interface CoverDao {
    @SqlUpdate(
        """
            insert into cover(metadata_source_id, metadata_source_type, url, ratio)
            values (:metadataSourceId, :metadataSourceType, :url, :ratio)
            on conflict (metadata_source_id, metadata_source_type) do update set
                url = excluded.url,
                ratio = excluded.ratio
        """,
    )
    fun upsertCover(
        metadataSourceId: String,
        metadataSourceType: MetadataSourceType,
        @BindBean cover: CoverMetadata,
    )
}

@Mapper(uses = [TimeMapper::class])
interface MovieDaoMapper {
    fun toModel(coverRow: CoverRow): CoverMetadata

    @Mapping(target = "id", source = "metadataRow.sourceId")
    @Mapping(target = "type", source = "metadataRow.sourceType")
    fun toModel(
        metadataRow: MetadataRow,
        cover: CoverMetadata?,
    ): MovieMetadata

    fun toModel(
        movieRow: MovieRow,
        metadata: List<MovieMetadata>,
    ): Movie {
        val metadata = metadata.associateBy { it.type }
        return Movie(
            id = movieRow.id,
            status = movieRow.status,
            imdbMetadata = metadata[MetadataSourceType.IMDB],
            tmdbMetadata = metadata[MetadataSourceType.TMDB],
        )
    }

    fun joinToMetadata(joinRow: JoinRow): MovieMetadata {
        val cover = joinRow.get(CoverRow::class.java)?.let(::toModel)
        val metadataRow = joinRow.get(MetadataRow::class.java)
        return toModel(metadataRow, cover)
    }
}

@Mapper
interface TimeMapper {
    fun toInstant(source: ZonedDateTime): Instant = source.toInstant()

    fun toZonedDateTime(source: Instant): ZonedDateTime = source.atZone(ZoneOffset.UTC)
}

@RegisterForReflection
data class MovieRow(
    var id: UUID,
    var status: MovieStatus,
)

@RegisterForReflection
data class MetadataRow(
    var sourceId: String,
    var sourceType: MetadataSourceType,
    var movieId: UUID,
    var title: String,
    var year: Int?,
    var rating: String?,
    var coverId: Long?,
    var infoPageUrl: String,
    var updateTime: ZonedDateTime,
)

@RegisterForReflection
data class CoverRow(
    var id: Long?,
    var url: String,
    var ratio: Double,
)
