package consulting.timhatdiehandandermaus.application.port

import consulting.timhatdiehandandermaus.application.model.MetadataSourceType
import consulting.timhatdiehandandermaus.application.model.MovieMetadata
import jakarta.inject.Qualifier

@Qualifier
@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MetadataSource(
    val source: MetadataSourceType,
)

interface MovieMetadataResolver {
    fun resolveByUrl(url: String): MovieMetadata

    fun resolveById(
        id: String,
        idSource: MetadataSourceType? = null,
    ): MovieMetadata
}
