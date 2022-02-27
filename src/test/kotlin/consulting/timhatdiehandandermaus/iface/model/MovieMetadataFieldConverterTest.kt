package consulting.timhatdiehandandermaus.iface.model

import consulting.timhatdiehandandermaus.iface.api.model.MovieMetadataField
import consulting.timhatdiehandandermaus.iface.api.model.MovieMetadataFieldConverter
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import javax.inject.Inject

@QuarkusTest
class MovieMetadataFieldConverterTest {

    @Inject
    lateinit var movieMetadataFieldConverter: MovieMetadataFieldConverter

    @ParameterizedTest
    @EnumSource(MovieMetadataField::class)
    fun testConversionToDomain(field: MovieMetadataField) {
        val mapped = assertDoesNotThrow { movieMetadataFieldConverter.toDomain(field) }
        assertEquals(field.name.lowercase(), mapped.name.lowercase())
    }
}
