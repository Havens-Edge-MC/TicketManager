package com.github.hoshikurama.ticketmanager.pipeline

import com.github.hoshikurama.ticketmanager.TMLocale
import com.github.hoshikurama.ticketmanager.data.GlobalPluginState
import com.github.hoshikurama.ticketmanager.data.InstancePluginState
import com.github.hoshikurama.ticketmanager.misc.pushErrors
import com.github.hoshikurama.ticketmanager.platform.PlatformFunctions
import com.github.hoshikurama.ticketmanager.platform.Sender
import com.github.hoshikurama.ticketmanager.ticket.User
import java.util.concurrent.CompletableFuture

abstract class HybridPipeline(
    val platform: PlatformFunctions,
    val instanceState: InstancePluginState,
    globalState: GlobalPluginState,
) : Pipeline {
    private val corePipeline = CorePipeline(platform, instanceState, globalState)

    override fun execute(sender: Sender, args: List<String>) {
        CompletableFuture.supplyAsync { corePipeline.executeLogic(sender, args) }
            .thenComposeAsync { it }
            .thenApplyAsync { notification ->
                notification?.run {

                    // Send notification to current server
                    CompletableFuture.runAsync {
                        if (sendSenderMSG)
                            generateSenderMSG(sender.locale).run(sender::sendMessage)

                        if (sendCreatorMSG && creator is User) {
                            val creatorPlayer = platform.buildPlayer((creator as User).uuid, instanceState.localeHandler)

                            if (creatorPlayer != null && creatorPlayer.has(creatorAlertPerm) && !creatorPlayer.has(massNotifyPerm))
                                generateCreatorMSG(creatorPlayer.locale).run(creatorPlayer::sendMessage)
                        }

                        if (sendMassNotify) {
                            platform.massNotify(instanceState.localeHandler, massNotifyPerm, generateMassNotify)
                        }
                    }.exceptionallyAsync {
                        pushErrors(platform, instanceState, it as Exception, TMLocale::consoleErrorCommandExecution)
                        sender.sendMessage(sender.locale.warningsUnexpectedError)
                        return@exceptionallyAsync null
                    }

                    // Send notification to other servers
                    CompletableFuture.runAsync {
                        platform.relayMessageToProxy(encodeForProxy())
                    }

                }
            }
    }
}