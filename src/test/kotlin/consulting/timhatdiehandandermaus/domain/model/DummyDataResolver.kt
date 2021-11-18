package consulting.timhatdiehandandermaus.domain.model

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver
import java.util.UUID
import kotlin.random.Random

class DummyDataResolver : ParameterResolver {
    private val supportedTypes = listOf(
        Movie::class,
        MovieMetadata::class,
    )

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return supportedTypes.contains(parameterContext.parameter.type.kotlin)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val name = parameterContext.parameter.name
        return when (parameterContext.parameter.type.kotlin) {
            Movie::class -> movie(name)
            MovieMetadata::class -> metadata(name)
            else -> throw ParameterResolutionException("Unsupported type: ${parameterContext.parameter.type}")
        }
    }

    private fun movie(name: String): Movie = Movie(
        id = UUID.randomUUID(),
        status = MovieStatus.Queued,
        metadata = metadata(name),
    )

    private fun metadata(name: String): MovieMetadata = MovieMetadata(
        id = "test-$name",
        title = "test-title-$name",
        year = Random.nextInt(1900, 3000),
        rating = String.format("%.1f", Random.nextFloat()),
        coverUrl = "https://m.media-amazon.com/images/M/MV5BMTkxMjYyNzgwMl5BMl5BanBnXkFtZTgwMTE3MjYyMTE@._V1_.jpg",
    )
}
