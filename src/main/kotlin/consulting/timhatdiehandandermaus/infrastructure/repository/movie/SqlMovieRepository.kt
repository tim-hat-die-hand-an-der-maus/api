package consulting.timhatdiehandandermaus.infrastructure.repository.movie

import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.transaction.Transactional

@RequestScoped
class SqlMovieRepository @Inject constructor(
    private val mapper: MovieEntityMapper,
) : MovieRepository, PanacheRepositoryBase<MovieEntity, UUID> {

    @Transactional
    @Throws(DuplicateMovieException::class)
    override fun insert(movie: MovieInsertDto): UUID {
        val entity = mapper.toEntity(movie)

        val existing = find("imdb_id", movie.metadata.id).firstResult()
        if (existing != null) {
            throw DuplicateMovieException()
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

    override fun findWithoutCoverUrl(): List<Movie> {
        return find("cover_url is NULL").list().map { mapper.toModel(it) }
    }
}

@Mapper
interface MovieEntityMapper {
    @Mapping(target = "id", ignore = true)
    fun toEntity(movie: MovieInsertDto): MovieEntity

    fun toEntity(movieMetadata: MovieMetadata): MovieMetadataEntity

    fun toModel(movieEntity: MovieEntity): Movie
}

@Entity
@Table(name = "movie")
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
    @Column(name = "cover_url")
    var coverUrl: String?,
)
