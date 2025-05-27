package consulting.timhatdiehandandermaus.infrastructure.repository.user

import com.radcortez.flyway.test.annotation.DataSource
import com.radcortez.flyway.test.annotation.FlywayTest
import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import consulting.timhatdiehandandermaus.domain.model.DummyDataResolver
import consulting.timhatdiehandandermaus.infrastructure.repository.QuarkusDataSourceProvider
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@QuarkusTest
@FlywayTest(DataSource(QuarkusDataSourceProvider::class))
@ExtendWith(DummyDataResolver::class)
class UserRepositoryTest {
    @Inject
    lateinit var movieRepo: MovieRepository

    @Inject
    lateinit var repo: QueueRepository

    @Test
    fun testInsert(metadata: MovieMetadata) {
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, tmdbMetadata = null))
        assertDoesNotThrow {
            repo.insert(id)
        }
    }

    @Test
    fun testInsertUnknownMovie() {
        assertThrows<MovieNotFoundException> {
            repo.insert(UUID.randomUUID())
        }
    }

    @Test
    fun testDuplicateInsert(metadata: MovieMetadata) {
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, tmdbMetadata = null))

        repo.insert(id)
        assertThrows<DuplicateMovieException> {
            repo.insert(id)
        }

        assertEquals(1, repo.list().size)
    }

    @Test
    fun testList(
        metadataOne: MovieMetadata,
        metadataTwo: MovieMetadata,
    ) {
        val ids =
            listOf(metadataOne, metadataTwo).map {
                movieRepo.insert(MovieInsertDto(MovieStatus.Queued, it, tmdbMetadata = null))
            }

        ids.forEach(repo::insert)

        val list = repo.list()
        assertEquals(ids, list.map { it.movieId })
    }

    @Test
    fun testDelete(metadata: MovieMetadata) {
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, null))

        repo.insert(id)
        assertDoesNotThrow {
            repo.delete(id)
        }

        assertTrue(repo.list().isEmpty())
    }

    @Test
    fun testDeleteUnknown() {
        assertThrows<MovieNotFoundException> {
            repo.delete(UUID.randomUUID())
        }
    }
}
