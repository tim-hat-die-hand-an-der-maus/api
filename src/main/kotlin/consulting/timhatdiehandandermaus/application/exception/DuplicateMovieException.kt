package consulting.timhatdiehandandermaus.application.exception

import java.util.UUID

class DuplicateMovieException(val id: UUID) : Exception()
