package consulting.timhatdiehandandermaus.application.exception

import java.util.UUID

open class DuplicateException(
    val id: UUID,
) : Exception()

class DuplicateMovieException(
    id: UUID,
) : DuplicateException(id)
