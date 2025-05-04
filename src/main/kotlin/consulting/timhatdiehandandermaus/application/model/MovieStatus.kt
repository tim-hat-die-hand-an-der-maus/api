package consulting.timhatdiehandandermaus.application.model

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
enum class MovieStatus {
    Queued,
    Watched,
    Deleted,
}
