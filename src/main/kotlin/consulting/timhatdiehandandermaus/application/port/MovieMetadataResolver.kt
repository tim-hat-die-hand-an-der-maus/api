package consulting.timhatdiehandandermaus.application.port

import consulting.timhatdiehandandermaus.application.exception.MovieNotFoundException
import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import jakarta.inject.Qualifier
import java.io.IOException

@Qualifier
@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MetadataSource(
    val source: MetadataSourceType,
)

interface MovieMetadataResolver {
    @Throws(IOException::class, MovieNotFoundException::class)
    fun resolveByUrl(url: String): MovieMetadata

    @Throws(IOException::class, MovieNotFoundException::class)
    fun resolveById(
        id: String,
        idSource: MetadataSourceType? = null,
    ): MovieMetadata
}
