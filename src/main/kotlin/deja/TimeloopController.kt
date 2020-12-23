package deja

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.time.Duration

object TimeloopController : Controller("timeloop") {
    val REPLAY_PACKET = Identifier("deja", "flashback.replay")
    val TIMELOOP_DURATION = Duration.ofMinutes(22).toTicks()
    var lastLoop = 0

    init {
        command("test") {
            does { replayNow(source.player) }
        }

        command("check") {
            does {
                respond("Timeloop Stats\n" +
                    "Current tick: ${source.minecraftServer.ticks}\n" +
                    "Last loop: $lastLoop"
                )
            }
        }
    }

    object ServerTick : ServerTickEvents.StartTick {
        override fun onStartTick(server: MinecraftServer) {
            if (server.ticks - lastLoop > TIMELOOP_DURATION) {
                lastLoop = server.ticks
                server.playerManager.playerList.forEach(TimeloopController::replayNow)
            }
        }
    }

    private fun replayNow(player: ServerPlayerEntity) = player.send(REPLAY_PACKET)
}
