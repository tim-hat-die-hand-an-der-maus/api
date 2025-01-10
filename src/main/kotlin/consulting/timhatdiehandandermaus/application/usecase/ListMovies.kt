package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
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
            movies: Iterable<Movie>,
            query: String,
        ): List<Movie> =
            movies.sortedByDescending {
                FuzzySearch.weightedRatio(it.metadata.title, query)
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
