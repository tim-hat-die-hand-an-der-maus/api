@file:OptIn(ExperimentalCli::class)

package consulting.timhatdiehandandermaus.iface.cli

import consulting.timhatdiehandandermaus.application.usecase.GenerateToken
import consulting.timhatdiehandandermaus.application.usecase.ShuffleQueue
import consulting.timhatdiehandandermaus.application.usecase.UpdateAllMetadata
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.enterprise.context.control.ActivateRequestContext
import jakarta.inject.Inject
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import java.nio.file.Path
import kotlin.io.path.writeText

private class GenerateTokenCommand(
    private val runWithRequestContext: (() -> Unit) -> Unit,
    private val generateToken: GenerateToken,
) : Subcommand(
        "generate-token",
        "Generate a JWT token",
    ) {
    val serviceName by argument(
        ArgType.String,
        "service-name",
        "The name of the service to issue a token for. Will be the subject claim.",
    )

    val outputFile: String by argument(ArgType.String, "output", "output path")

    override fun execute() {
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

private class RunApiCommand :
    Subcommand(
        "run-api",
        "Serve the API",
    ) {
    override fun execute() {
        Quarkus.waitForExit()
    }
}

private class ShuffleQueueCommand(
    private val runWithRequestContext: (() -> Unit) -> Unit,
    private val shuffleQueue: ShuffleQueue,
) : Subcommand(
        "shuffle-queue",
        "Shuffle the current queue",
    ) {
    override fun execute() {
        runWithRequestContext {
            shuffleQueue()
        }
    }
}

private class UpdateMetadataCommand(
    private val runWithRequestContext: (() -> Unit) -> Unit,
    private val updateAllMetadata: UpdateAllMetadata,
) : Subcommand(
        "update-metadata",
        "Updates metadata of all movies in DB",
    ) {
    override fun execute() {
        runWithRequestContext {
            updateAllMetadata()
        }
    }
}

@QuarkusMain
class Main
    @Inject
    constructor(
        private val generateToken: GenerateToken,
        private val shuffleQueue: ShuffleQueue,
        private val updateAllMetadata: UpdateAllMetadata,
    ) : QuarkusApplication {
        @OptIn(ExperimentalCli::class)
        override fun run(args: Array<String>): Int {
            val parser = ArgParser("application")

            parser.subcommands(
                GenerateTokenCommand(this::withRequestContext, generateToken),
                RunApiCommand(),
                ShuffleQueueCommand(this::withRequestContext, shuffleQueue),
                UpdateMetadataCommand(this::withRequestContext, updateAllMetadata),
            )

            parser.parse(args)

            return 0
        }

        @ActivateRequestContext
        fun withRequestContext(action: () -> Unit) {
            action()
        }
    }
