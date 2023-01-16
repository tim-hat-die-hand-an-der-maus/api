package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import me.xdrop.fuzzywuzzy.FuzzySearch
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class ListMovies @Inject constructor(
    private val movieRepository: MovieRepository,
) {

    private fun rank(movies: Iterable<Movie>, query: String): List<Movie> {
        return movies.sortedByDescending {
            FuzzySearch.weightedRatio(it.metadata.title, query)
        }
    }

    operator fun invoke(query: String?, status: MovieStatus?): List<Movie> {
        val results = movieRepository.listMovies(status)
        return if (query == null) {
            results.toList()
        } else {
            rank(results, query)
        }
    }
}
