package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.model.Movie
import consulting.timhatdiehandandermaus.application.model.MovieStatus
import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import me.xdrop.fuzzywuzzy.FuzzySearch

@RequestScoped
class ListMovies
    @Inject
    constructor(
        private val movieRepository: MovieRepository,
    ) {
        private fun rank(
            movies: List<Movie>,
            query: String,
        ): List<Movie> =
            movies
                .sortedByDescending {
                    val meta = (it.tmdbMetadata ?: it.imdbMetadata)!!
                    FuzzySearch.weightedRatio(meta.title, query)
                }

        operator fun invoke(
            query: String?,
            status: MovieStatus?,
        ): List<Movie> {
            val results = movieRepository.listMovies(status)
            return if (query.isNullOrBlank()) {
                results.toList()
            } else {
                rank(results, query)
            }
        }
    }
