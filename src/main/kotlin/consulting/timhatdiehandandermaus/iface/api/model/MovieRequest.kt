// ktlint-disable filename

package consulting.timhatdiehandandermaus.iface.api.model

import com.fasterxml.jackson.annotation.JsonCreator

data class MoviePostRequest @JsonCreator constructor(val imdbUrl: String)
