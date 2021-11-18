package consulting.timhatdiehandandermaus.application.usecase

import com.radcortez.flyway.test.annotation.DataSource
import com.radcortez.flyway.test.annotation.FlywayTest
import consulting.timhatdiehandandermaus.application.port.MovieMetadataResolver
import consulting.timhatdiehandandermaus.application.repository.MovieInsertDto
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.DummyDataResolver
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieMetadata
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import consulting.timhatdiehandandermaus.infrastructure.repository.QuarkusDataSourceProvider
import io.quarkus.arc.AlternativePriority
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.persistence.EntityManager

@QuarkusTest
@FlywayTest(DataSource(QuarkusDataSourceProvider::class))
@ExtendWith(DummyDataResolver::class)
class FindMissingCoversTest {
    @Inject
    lateinit var findMissingCovers: FindMissingCovers

    @Inject
    lateinit var entityManager: EntityManager

    @Inject
    lateinit var movieRepo: MovieRepository

    @Test
    fun `empty database`() {
        assertDoesNotThrow {
            findMissingCovers()
        }
    }

    @Test
    fun `happy path`(metadata: MovieMetadata) {
        val coverlessMetadata = metadata.copy(
            coverUrl = null,
        )
        val movieId = movieRepo.insert(MovieInsertDto(MovieStatus.Queued, coverlessMetadata))

        assertDoesNotThrow {
            findMissingCovers()
        }

        entityManager.clear()
        val movie = movieRepo.find(movieId)
        assertNotNull(movie)
        movie as Movie
        assertEquals("https://example.com/image.jpg", movie.metadata.coverUrl)
    }

    @AlternativePriority(1)
    @RequestScoped
    class MockMetadataResolver : MovieMetadataResolver {
        override fun resolveImdb(imdbUrl: String): MovieMetadata = resolveImdbById(imdbUrl)

        override fun resolveImdbById(imdbId: String): MovieMetadata {
            return MovieMetadata(
                id = imdbId,
                title = "mock-title",
                2022,
                rating = "10.0",
                coverUrl = "https://example.com/image.jpg",
            )
        }
    }
}
