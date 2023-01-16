package consulting.timhatdiehandandermaus.application.usecase

import consulting.timhatdiehandandermaus.application.repository.MovieRepository
import consulting.timhatdiehandandermaus.domain.model.Movie
import consulting.timhatdiehandandermaus.domain.model.MovieStatus
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class ListMovies @Inject constructor(
    private val movieRepository: MovieRepository,
) {
    operator fun invoke(status: MovieStatus): List<Movie> {
        return movieRepository.listMovies(status).toList()
    }
}
