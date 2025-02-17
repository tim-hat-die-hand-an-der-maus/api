package consulting.timhatdiehandandermaus.iface.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import consulting.timhatdiehandandermaus.application.usecase.GenerateToken
import consulting.timhatdiehandandermaus.application.usecase.ShuffleQueue
import consulting.timhatdiehandandermaus.application.usecase.UpdateAllMetadata
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.enterprise.context.control.ActivateRequestContext
import jakarta.inject.Inject
import java.nio.file.Path
import kotlin.io.path.writeText

private class GenerateTokenCommand(
    private val runWithRequestContext: (() -> Unit) -> Unit,
    private val generateToken: GenerateToken,
) : CliktCommand("generate-token") {
    override fun help(context: Context) = "Generate a JWT token"

    val serviceName by argument(
        "service-name",
        "The name of the service to issue a token for. Will be the subject claim.",
    )

    val outputFile: String by argument("output", "output path")

    override fun run() {
        runWithRequestContext {
            val path = Path.of(outputFile)
            if (!path.isAbsolute) {
                throw (IllegalArgumentException("outputFile path must be absolute"))
            }
            val token = generateToken(serviceName)
            path.writeText(token)
        }
    }
}

private class RunApiCommand : CliktCommand("run-api") {
    override fun help(context: Context) = "Serve the API"

    override fun run() {
        Quarkus.waitForExit()
    }
}

private class ShuffleQueueCommand(
    private val runWithRequestContext: (() -> Unit) -> Unit,
    private val shuffleQueue: ShuffleQueue,
) : CliktCommand("shuffle-queue") {
    override fun help(context: Context) = "Shuffle the current queue"

    override fun run() {
        runWithRequestContext {
            shuffleQueue()
        }
    }
}

private class UpdateMetadataCommand(
    private val runWithRequestContext: (() -> Unit) -> Unit,
    private val updateAllMetadata: UpdateAllMetadata,
) : CliktCommand("update-metadata") {
    override fun help(context: Context) = "Updates metadata of all movies in DB"

    override fun run() {
        runWithRequestContext {
            updateAllMetadata()
        }
    }
}

private class MainCommand : CliktCommand("main") {
    override fun run() = Unit
}

@QuarkusMain
class Main
    @Inject
    constructor(
        private val generateToken: GenerateToken,
        private val shuffleQueue: ShuffleQueue,
        private val updateAllMetadata: UpdateAllMetadata,
    ) : QuarkusApplication {
        override fun run(args: Array<String>): Int {
            val main =
                MainCommand()
                    .subcommands(
                        GenerateTokenCommand(this::withRequestContext, generateToken),
                        RunApiCommand(),
                        ShuffleQueueCommand(this::withRequestContext, shuffleQueue),
                        UpdateMetadataCommand(this::withRequestContext, updateAllMetadata),
                    )

            main.main(args)

            return 0
        }

        @ActivateRequestContext
        fun withRequestContext(action: () -> Unit) {
            action()
        }
    }
