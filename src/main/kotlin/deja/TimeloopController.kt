package deja

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.time.Duration

/**
 * Timeloop controller
 */
object TimeloopController : Controller("timeloop") {
    /**
     * Packet for requesting clients play their flashback
     */
    val REPLAY_PACKET = Identifier("deja", "flashback.replay")
    private val TIMELOOP_DURATION = Duration.ofMinutes(22).toTicks()
    private var lastLoop = 0

    init {
        command("test") {
            does { replayNow(source.player) }
        }

        command("check") {
            does {
                respond(
                    "Timeloop Stats\n" +
                        "Current tick: ${source.minecraftServer.ticks}\n" +
                        "Last loop: $lastLoop"
                )
            }
        }
    }

    /**
     * Timeloop server tick handler
     */
    object ServerTick : ServerTickEvents.StartTick {
        /**
         * On tick
         */
        override fun onStartTick(server: MinecraftServer) {
            if (server.ticks - lastLoop > TIMELOOP_DURATION) {
                lastLoop = server.ticks
                server.playerManager.playerList.forEach(TimeloopController::replayNow)
            }
        }
    }

    private fun replayNow(player: ServerPlayerEntity) = player.send(REPLAY_PACKET)
}
