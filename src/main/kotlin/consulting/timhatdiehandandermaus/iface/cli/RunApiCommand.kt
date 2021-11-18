package consulting.timhatdiehandandermaus.iface.cli

import io.quarkus.picocli.runtime.annotations.TopCommand
import io.quarkus.runtime.Quarkus
import picocli.CommandLine

@TopCommand
@CommandLine.Command(
    mixinStandardHelpOptions = true,
    subcommands = [
        FindMissingCoversCommand::class,
    ],
)
class RunApiCommand : Runnable {
    override fun run() {
        Quarkus.waitForExit()
    }
}
