package consulting.timhatdiehandandermaus.domain.model

import consulting.timhatdiehandandermaus.application.model.CoverMetadata
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

annotation class Timestamped(
    val time: String,
)

annotation class TestMetadataSource(
    val source: MetadataSourceType,
)

class DummyDataResolver : ParameterResolver {
    private val clock = Clock.tickMillis(ZoneOffset.UTC)

    private val supportedTypes =
        listOf(
            Movie::class,
            MovieMetadata::class,
        )

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Boolean = supportedTypes.contains(parameterContext.parameter.type.kotlin)

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Any {
        val name = parameterContext.parameter.name

        val timestamped = parameterContext.findAnnotation(Timestamped::class.java).getOrNull()
        val updateTime = timestamped?.time?.let(Instant::parse)

        val source = parameterContext.findAnnotation(TestMetadataSource::class.java).getOrNull()

        return when (parameterContext.parameter.type.kotlin) {
            Movie::class -> movie(name)
            MovieMetadata::class -> metadata(name, updateTime ?: Instant.now(clock), source?.source ?: MetadataSourceType.IMDB)
            else -> throw ParameterResolutionException("Unsupported type: ${parameterContext.parameter.type}")
        }
    }

    private fun movie(name: String): Movie =
        Movie(
            id = UUID.randomUUID(),
            status = MovieStatus.Queued,
            imdbMetadata = metadata(name, updateTime = Instant.now(clock), source = MetadataSourceType.IMDB),
            tmdbMetadata = metadata(name, updateTime = Instant.now(clock), source = MetadataSourceType.TMDB),
        )

    private fun metadata(
        name: String,
        updateTime: Instant,
        source: MetadataSourceType,
    ): MovieMetadata =
        MovieMetadata(
            type = source,
            id = "test-$name",
            title = "test-title-$name",
            year = Random.nextInt(1900, 3000),
            rating = String.format("%.1f", Random.nextFloat()),
            cover =
                CoverMetadata(
                    url = "https://m.media-amazon.com/images/M/MV5BMTkxMjYyNzgwMl5BMl5BanBnXkFtZTgwMTE3MjYyMTE@._V1_.jpg",
                    ratio = 0.5,
                ),
            infoPageUrl = "https://www.imdb.com/title/tt123456",
            updateTime = updateTime,
        )
}
