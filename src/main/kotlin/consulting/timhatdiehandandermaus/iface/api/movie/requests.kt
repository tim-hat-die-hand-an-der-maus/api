// ktlint-disable filename

package consulting.timhatdiehandandermaus.iface.api.movie

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class MoviePostRequest(val imdbUrl: String)
