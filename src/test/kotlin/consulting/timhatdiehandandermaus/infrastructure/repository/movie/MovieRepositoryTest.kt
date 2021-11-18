package consulting.timhatdiehandandermaus.infrastructure.repository.movie

import com.radcortez.flyway.test.annotation.DataSource
import com.radcortez.flyway.test.annotation.FlywayTest
import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.DummyDataResolver
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import consulting.timhatdiehandandermaus.infrastructure.repository.QuarkusDataSourceProvider
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Inject

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
    fun testUpdateMetadata(metadata: MovieMetadata, newMetadata: MovieMetadata) {
        val id = repo.insert(MovieInsertDto(MovieStatus.Queued, metadata))

        repo.updateMetadata(id, newMetadata)

        val persisted = repo.find(id)
        assertNotNull(persisted)
        assertEquals(persisted!!.metadata, newMetadata)
    }
}
