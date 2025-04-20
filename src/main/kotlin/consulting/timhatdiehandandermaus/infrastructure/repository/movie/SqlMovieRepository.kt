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
import consulting.timhatdiehandandermaus.infrastructure.repository.jooq.Tables
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.narayana.jta.runtime.TransactionConfiguration
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.transaction.Transactional
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID


@RequestScoped
class SqlMovieRepository
    @Inject
    constructor(
        private val mapper: MovieEntityMapper,
    ) : MovieRepository,
        PanacheRepositoryBase<MovieEntity, UUID> {
        @Transactional
        @Throws(DuplicateMovieException::class)
        override fun insert(movie: MovieInsertDto): UUID {
            val entity = mapper.toEntity(movie)

            for (meta in entity.metadata) {
                meta.movie = entity
                val existing =
                    find(
                        "SELECT DISTINCT m FROM MovieEntity m JOIN m.metadata meta WHERE meta.sourceId = ?1 AND meta.sourceType = ?2",
                        meta.sourceId,
                        meta.sourceType,
                    ).firstResult()
                if (existing != null) {
                    throw DuplicateMovieException(existing.id!!)
                }
            }

            persist(entity)

            return entity.id!!
        }

        @Transactional
        @Throws(MovieNotFoundException::class)
        override fun updateMetadata(
            id: UUID,
            metadata: MovieMetadata,
        ) {
            val movie = findById(id) ?: throw MovieNotFoundException()

            var metadataEntity = mapper.toEntity(metadata)

            // This is so stupid.
            metadataEntity.movie = movie

            // This is so stupid.
            getEntityManager().merge(metadataEntity)

            persist(movie)
        }

        @Transactional
        @Throws(MovieNotFoundException::class)
        override fun updateStatus(
            id: UUID,
            status: MovieStatus,
        ) {
            val persisted = findById(id) ?: throw MovieNotFoundException()
            persisted.status = status
            persist(persisted)
        }

        override fun find(id: UUID): Movie? = findById(id)?.let(mapper::toModel)

        @Transactional
        @TransactionConfiguration(timeout = 3600)
        override fun forEachMovie(
            metadataUpdateTimeCutoff: Instant?,
            limit: Long,
            action: (Movie) -> Unit,
        ) {
            var stream =
                if (metadataUpdateTimeCutoff == null) {
                    streamAll()
                } else {
                    stream(
                        "SELECT DISTINCT m FROM MovieEntity m JOIN m.metadata meta WHERE meta.updateTime < ?1",
                        metadataUpdateTimeCutoff.atZone(ZoneOffset.UTC),
                    )
                }

            if (limit > 0) {
                stream = stream.limit(limit)
            }

            stream.map(mapper::toModel).forEach(action)
        }

        override fun listMovies(status: MovieStatus?): Iterable<Movie> {
            val list =
                if (status == null) {
                    listAll()
                } else {
                    list("status", status)
                }
            return list.map(mapper::toModel)
        }
    }

@Mapper(uses = [TimeMapper::class])
interface MovieEntityMapper {
    fun toEntity(movie: MovieInsertDto): MovieEntity {
        val metadata = mutableSetOf<MovieMetadataEntity>()



        movie.imdbMetadata?.let(::toEntity)?.let(metadata::add)
        movie.tmdbMetadata?.let(::toEntity)?.let(metadata::add)

        return MovieEntity(
            status = movie.status,
            metadata = metadata,
        )
    }

    @Mapping(target = "sourceId", source = "id")
    @Mapping(target = "sourceType", source = "type")
    @Mapping(
        target = "updateTime",
        expression = "java(movieMetadata.getUpdateTime().atZone(java.time.ZoneOffset.UTC))",
    )
    @Mapping(target = "movie", ignore = true)
    fun toEntity(movieMetadata: MovieMetadata): MovieMetadataEntity

    @Mapping(target = "id", ignore = true)
    fun toEntity(coverMetadata: CoverMetadata): CoverEntity

    @Mapping(target = "id", source = "sourceId")
    @Mapping(target = "type", source = "sourceType")
    fun toModel(movieMetadataEntity: MovieMetadataEntity): MovieMetadata

    fun toModel(movieEntity: MovieEntity): Movie {
        val metadata = movieEntity.metadata.associateBy { it.sourceType }
        return Movie(
            id = movieEntity.id!!,
            status = movieEntity.status,
            imdbMetadata = metadata[MetadataSourceType.IMDB]?.let(::toModel),
            tmdbMetadata = metadata[MetadataSourceType.TMDB]?.let(::toModel),
        )
    }
}

@Mapper
interface TimeMapper {
    fun toInstant(source: ZonedDateTime): Instant = source.toInstant()

    fun toZonedDateTime(source: Instant): ZonedDateTime = source.atZone(ZoneOffset.UTC)
}

@Entity
@Table(name = "movie")
@RegisterForReflection(targets = [Array<UUID>::class])
class MovieEntity(
    @Id
    @GeneratedValue
    var id: UUID? = null,
    @Enumerated(EnumType.STRING)
    var status: MovieStatus,
    @OneToMany(mappedBy = "movie", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var metadata: MutableSet<MovieMetadataEntity>,
)

@Entity
@Table(name = "metadata")
@RegisterForReflection(targets = [Array<UUID>::class])
class MovieMetadataEntity(
    @Id
    @Column(name = "source_id")
    var sourceId: String,
    @Id
    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    var sourceType: MetadataSourceType,
    var title: String,
    var year: Int?,
    var rating: String?,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "cover_id")
    var cover: CoverEntity?,
    @Column(name = "info_page_url")
    var infoPageUrl: String,
    @Column(name = "update_time")
    var updateTime: ZonedDateTime,
) {
    @ManyToOne
    @JoinColumn(name = "movie_id", referencedColumnName = "id", nullable = false)
    lateinit var movie: MovieEntity
}

@Entity
@Table(name = "cover")
class CoverEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var url: String,
    var ratio: Double,
)
