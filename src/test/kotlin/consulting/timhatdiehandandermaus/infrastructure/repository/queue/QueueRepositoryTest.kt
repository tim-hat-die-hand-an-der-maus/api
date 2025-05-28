package consulting.timhatdiehandandermaus.infrastructure.repository.queue

import com.radcortez.flyway.test.annotation.DataSource
import com.radcortez.flyway.test.annotation.FlywayTest
import consulting.timhatdiehandandermaus.application.exception.DuplicateMovieException
import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.CanonicalUser
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.application.repository.QueueRepository
import consulting.timhatdiehandandermaus.application.repository.UserRepository
import consulting.timhatdiehandandermaus.domain.model.DummyDataResolver
import consulting.timhatdiehandandermaus.infrastructure.repository.QuarkusDataSourceProvider
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@QuarkusTest
@FlywayTest(DataSource(QuarkusDataSourceProvider::class))
@ExtendWith(DummyDataResolver::class)
class QueueRepositoryTest {
    @Inject
    lateinit var movieRepo: MovieRepository

    @Inject
    lateinit var userRepo: UserRepository

    @Inject
    lateinit var repo: QueueRepository

    @Test
    fun testInsertNoUser(metadata: MovieMetadata) {
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, tmdbMetadata = null))
        assertDoesNotThrow {
            repo.insert(id, userId = null)
        }
    }

    @Test
    fun testInsertWithUser(
        metadata: MovieMetadata,
        user: CanonicalUser,
    ) {
        userRepo.insert(user)
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, tmdbMetadata = null))
        assertDoesNotThrow {
            repo.insert(id, userId = user.id)
        }
    }

    @Test
    fun testInsertUnknownMovie() {
        assertThrows<MovieNotFoundException> {
            repo.insert(UUID.randomUUID(), userId = null)
        }
    }

    @Test
    fun testInsertUnknownUser(metadata: MovieMetadata) {
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, tmdbMetadata = null))
        assertThrows<MovieNotFoundException> {
            repo.insert(movieId = id, userId = UUID.randomUUID())
        }
    }

    @Test
    fun testDuplicateInsert(metadata: MovieMetadata) {
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, tmdbMetadata = null))

        repo.insert(id, null)
        assertThrows<DuplicateMovieException> {
            repo.insert(id, null)
        }

        Assertions.assertEquals(1, repo.list().size)
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

        ids.forEach { repo.insert(it, null) }

        val list = repo.list()
        Assertions.assertEquals(ids, list.map { it.movieId })
    }

    @Test
    fun testDelete(metadata: MovieMetadata) {
        val id = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, metadata, null))

        repo.insert(id, userId = null)
        assertDoesNotThrow {
            repo.delete(id)
        }

        Assertions.assertTrue(repo.list().isEmpty())
    }

    @Test
    fun testDeleteUnknown() {
        assertThrows<MovieNotFoundException> {
            repo.delete(UUID.randomUUID())
        }
    }
}
