package consulting.timhatdiehandandermaus.domain.value

enum class MovieMetadataField {
    Cover,
    @Deprecated("Use Cover instead")
    CoverUrl,
    Rating,
    ;
}
