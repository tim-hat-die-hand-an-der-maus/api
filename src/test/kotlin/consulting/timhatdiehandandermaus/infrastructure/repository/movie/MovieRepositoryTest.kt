package consulting.timhatdiehandandermaus.infrastructure.repository.movie

import com.radcortez.flyway.test.annotation.DataSource
import com.radcortez.flyway.test.annotation.FlywayTest
import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.DummyDataResolver
import consulting.timhatdiehandandermaus.infrastructure.repository.QuarkusDataSourceProvider
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
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
        assertEquals(metadata, movie.metadata)
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

        repo.updateMetadata(id, newMetadata)

        val persisted = repo.find(id)
        assertNotNull(persisted)
        assertEquals(persisted!!.metadata, newMetadata)
    }

    @Test
    fun testForEachMovie(
        metadata1: MovieMetadata,
        metadata2: MovieMetadata,
        metadata3: MovieMetadata,
    ) {
        val metadata = setOf(metadata1, metadata2, metadata3)
        val ids = metadata.map { repo.insert(MovieInsertDto(MovieStatus.Queued, it)) }.toSet()

        val all: MutableSet<Movie> = mutableSetOf()
        repo.forEachMovie { all.add(it) }
        assertEquals(ids, all.map { it.id }.toSet())
        assertEquals(metadata, all.map { it.metadata }.toSet())
    }

    @Test
    fun testForEachMovieWithCutoff(
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
                            metadataUpdateTime =
                                if (it.id ==
                                    metadata1.id
                                ) {
                                    Instant.now().minus(
                                        Duration.ofDays(2),
                                    )
                                } else {
                                    Instant.now()
                                },
                        ),
                    )
                }.toSet()

        val all: MutableSet<Movie> = mutableSetOf()
        repo.forEachMovie(Instant.now().minusSeconds(60)) { all.add(it) }

        assertEquals(1, all.map { it.id }.size)
        assertEquals(setOf(metadata1), all.map { it.metadata }.toSet())
    }
}
