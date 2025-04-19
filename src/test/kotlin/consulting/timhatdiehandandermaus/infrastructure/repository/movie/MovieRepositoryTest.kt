package consulting.timhatdiehandandermaus.infrastructure.repository.movie

import com.radcortez.flyway.test.annotation.DataSource
import com.radcortez.flyway.test.annotation.FlywayTest
import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.DummyDataResolver
import consulting.timhatdiehandandermaus.domain.model.TestMetadataSource
import consulting.timhatdiehandandermaus.domain.model.Timestamped
import consulting.timhatdiehandandermaus.infrastructure.repository.QuarkusDataSourceProvider
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@QuarkusTest
@FlywayTest(DataSource(QuarkusDataSourceProvider::class))
@ExtendWith(DummyDataResolver::class)
class MovieRepositoryTest {
    @Inject
    lateinit var repo: MovieRepository

    @Test
    fun testInsert(metadata: MovieMetadata) {
        val id = repo.insert(MovieInsertDto(MovieStatus.Queued, metadata))
        assertNotNull(id)

        val movie = repo.find(id)
        assertNotNull(movie)
        assertEquals(id, movie!!.id)
        assertEquals(MovieStatus.Queued, movie.status)
        assertEquals(metadata, movie.imdbMetadata)
    }

    @Test
    fun testDuplicateInsert(metadata: MovieMetadata) {
        val dto = MovieInsertDto(MovieStatus.Queued, metadata)
        val id = repo.insert(dto)
        assertNotNull(id)

        assertThrows<DuplicateMovieException> {
            repo.insert(dto)
        }
    }

    @Test
    fun testUpdateStatus(metadata: MovieMetadata) {
        val id = repo.insert(MovieInsertDto(MovieStatus.Queued, metadata))

        repo.updateStatus(id, MovieStatus.Watched)

        val persisted = repo.find(id)
        assertNotNull(persisted)
        assertEquals(persisted!!.status, MovieStatus.Watched)
    }

    @Test
    fun testUpdateMetadata(
        metadata: MovieMetadata,
        newMetadata: MovieMetadata,
    ) {
        val id = repo.insert(MovieInsertDto(MovieStatus.Queued, metadata))

        val newMetadata = newMetadata.copy(id = metadata.id)
        repo.updateMetadata(id, newMetadata)

        val persisted = repo.find(id)
        assertNotNull(persisted)
        assertEquals(persisted!!.imdbMetadata, newMetadata)
    }

    @Test
    fun testUpdateMetadataWithoutOldMetadata(
        metadata: MovieMetadata,
        @TestMetadataSource(MetadataSourceType.TMDB)
        tmdbMetadata: MovieMetadata,
    ) {
        val id = repo.insert(MovieInsertDto(MovieStatus.Queued, metadata))
        repo.updateMetadata(id, tmdbMetadata)

        val persisted = repo.find(id)
        assertNotNull(persisted)
        assertEquals(persisted!!.tmdbMetadata, tmdbMetadata)
    }

    @Test
    fun testForEachMovie(
        @Timestamped("2020-01-01T00:00:00Z")
        metadata1: MovieMetadata,
        @Timestamped("2021-01-01T00:00:00Z")
        metadata2: MovieMetadata,
        @Timestamped("2022-01-01T00:00:00Z")
        metadata3: MovieMetadata,
    ) {
        val metadata = setOf(metadata1, metadata2, metadata3)
        val ids = metadata.map { repo.insert(MovieInsertDto(MovieStatus.Queued, it)) }.toSet()

        val all: MutableSet<Movie> = mutableSetOf()
        repo.forEachMovie { all.add(it) }
        assertEquals(ids, all.map { it.id }.toSet())
        assertEquals(metadata, all.map { it.imdbMetadata }.toSet())
    }

    @Test
    fun testForEachMovieWithCutoff(
        @Timestamped("2023-01-01T00:00:00Z")
        metadata1: MovieMetadata,
        metadata2: MovieMetadata,
        metadata3: MovieMetadata,
    ) {
        val metadata = setOf(metadata1, metadata2, metadata3)
        val ids =
            metadata
                .map {
                    repo.insert(
                        MovieInsertDto(
                            MovieStatus.Queued,
                            it,
                        ),
                    )
                }.toSet()

        val all: MutableSet<Movie> = mutableSetOf()
        repo.forEachMovie(Instant.now().minusSeconds(60)) { all.add(it) }

        assertEquals(1, all.map { it.id }.size)
        assertEquals(setOf(metadata1), all.map { it.imdbMetadata }.toSet())
    }
}
