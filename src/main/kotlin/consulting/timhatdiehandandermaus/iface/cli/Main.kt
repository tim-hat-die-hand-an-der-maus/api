package consulting.timhatdiehandandermaus.iface.cli

import consulting.timhatdiehandandermaus.application.usecase.ShuffleQueue
import consulting.timhatdiehandandermaus.application.usecase.UpdateAllMetadata
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import javax.enterprise.context.control.ActivateRequestContext
import javax.inject.Inject

enum class Command {
    RunApi,
    ShuffleQueue,
    UpdateMetadata,
    ;

    override fun toString(): String {
        return name
            .replaceFirstChar { it.lowercaseChar() }
            .replace(Regex("[A-Z]")) { "-${it.value.lowercase()}" }
    }

    companion object {
        fun ofString(s: String): Command {
            return values().first { it.toString() == s }
        }
    }
}

@QuarkusMain
class Main @Inject constructor(
    private val shuffleQueue: ShuffleQueue,
    private val updateAllMetadata: UpdateAllMetadata,
) : QuarkusApplication {

    override fun run(args: Array<String>): Int {
        val parser = ArgParser("application")
        val command: Command by parser.argument(
            ArgType.Choice(
                toString = { it.toString() },
                toVariant = { Command.ofString(it) },
            ),
        )

        parser.parse(args)

        when (command) {
            Command.RunApi -> Quarkus.waitForExit()
            Command.ShuffleQueue -> runShuffleQueue()
            Command.UpdateMetadata -> runUpdateMetadata()
        }

        return 0
    }

    @ActivateRequestContext
    fun runUpdateMetadata() = updateAllMetadata()

    @ActivateRequestContext
    fun runShuffleQueue() = shuffleQueue()
}
