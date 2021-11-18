package consulting.timhatdiehandandermaus.iface.cli

import consulting.timhatdiehandandermaus.application.usecase.FindMissingCovers
import picocli.CommandLine
import javax.enterprise.context.control.ActivateRequestContext
import javax.inject.Inject

@CommandLine.Command(name = "find-missing-covers")
class FindMissingCoversCommand : Runnable {
    @Inject
    lateinit var findMissingCovers: FindMissingCovers

    @ActivateRequestContext
    override fun run() {
        findMissingCovers()
    }
}
