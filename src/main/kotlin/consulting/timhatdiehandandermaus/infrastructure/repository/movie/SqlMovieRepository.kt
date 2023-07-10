package consulting.timhatdiehandandermaus.infrastructure.repository.movie

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.CoverMetadata
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.narayana.jta.runtime.TransactionConfiguration
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.transaction.Transactional
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.util.UUID

@RequestScoped
class SqlMovieRepository @Inject constructor(
    private val mapper: MovieEntityMapper,
) : MovieRepository, PanacheRepositoryBase<MovieEntity, UUID> {

    @Transactional
    @Throws(DuplicateMovieException::class)
    override fun insert(movie: MovieInsertDto): UUID {
        val entity = mapper.toEntity(movie)

        val existing = find("metadata.id", movie.metadata.id).firstResult()
        if (existing != null) {
            throw DuplicateMovieException(existing.id!!)
        }

        persist(entity)

        return entity.id!!
    }

    @Transactional
    @Throws(MovieNotFoundException::class)
    override fun updateMetadata(id: UUID, metadata: MovieMetadata) {
        val persisted = findById(id) ?: throw MovieNotFoundException()
        persisted.metadata = mapper.toEntity(metadata)
        persist(persisted)
    }

    @Transactional
    @Throws(MovieNotFoundException::class)
    override fun updateStatus(id: UUID, status: MovieStatus) {
        val persisted = findById(id) ?: throw MovieNotFoundException()
        persisted.status = status
        persist(persisted)
    }

    override fun find(id: UUID): Movie? {
        return findById(id)?.let(mapper::toModel)
    }

    @Transactional
    @TransactionConfiguration(timeout = 3600)
    override fun forEachMovie(action: (Movie) -> Unit) {
        streamAll().map(mapper::toModel).forEach(action)
    }

    override fun listMovies(status: MovieStatus?): Iterable<Movie> {
        val list = if (status == null) {
            listAll()
        } else {
            list("status", status)
        }
        return list.map(mapper::toModel)
    }
}

@Mapper
interface MovieEntityMapper {
    @Mapping(target = "id", ignore = true)
    fun toEntity(movie: MovieInsertDto): MovieEntity

    fun toEntity(movieMetadata: MovieMetadata): MovieMetadataEntity
    fun toEntity(coverMetadata: CoverMetadata): CoverMetadataEntity

    fun toModel(movieEntity: MovieEntity): Movie
}

@Entity
@Table(name = "movie")
@RegisterForReflection(targets = [UUID::class])
class MovieEntity {
    @Id
    @GeneratedValue
    var id: UUID? = null

    @Enumerated(EnumType.STRING)
    lateinit var status: MovieStatus

    @Embedded
    lateinit var metadata: MovieMetadataEntity
}

@Embeddable
class MovieMetadataEntity(
    @Column(name = "imdb_id", unique = true)
    var id: String,
    var title: String,
    var year: Int,
    var rating: String,
    @Embedded
    var cover: CoverMetadataEntity,
)

@Embeddable
class CoverMetadataEntity(
    @Column(name = "cover_url")
    var url: String,
    @Column(name = "cover_ratio")
    var ratio: Double,
)
