package consulting.timhatdiehandandermaus.iface.cli

import consulting.timhatdiehandandermaus.application.usecase.FindMissingCovers
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import org.jboss.logging.Logger
import javax.enterprise.context.control.ActivateRequestContext
import javax.inject.Inject

@QuarkusMain
class Main @Inject constructor(
    private val log: Logger,
    private val findMissingCovers: FindMissingCovers,
) : QuarkusApplication {

    override fun run(vararg args: String): Int {
        if (args.isEmpty()) {
            Quarkus.waitForExit()
            return 0
        }

        if (args[0] == "find-missing-covers") {
            return runFindMissingCovers()
        }

        log.error("Unrecognized args: ${args.contentToString()}")
        return 1
    }

    @ActivateRequestContext
    fun runFindMissingCovers(): Int {
        findMissingCovers()
        return 0
    }
}
