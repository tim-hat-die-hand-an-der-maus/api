package consulting.timhatdiehandandermaus.application.usecase

import org.jboss.logging.Logger
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

@RequestScoped
class FindMissingCovers @Inject constructor(
    private val log: Logger
) {
    operator fun invoke() {
        log.info("You successfully called the find-missing-covers command! Now implement it.")
    }
}
