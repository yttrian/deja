package deja

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object FlashbackController : Controller("flashback") {
    private const val SNAP_TICK_FREQUENCY = 50

    val SNAP_PACKET = Identifier("deja", "flashback.snap")
    val REPLAY_PACKET = Identifier("deja", "flashback.replay")

    init {
        command("snap") {
            does { snapNow(source.player) }
        }

        command("replay") {
            does { replayNow(source.player) }
        }
    }

    object ServerTick : ServerTickEvents.StartTick {
        override fun onStartTick(server: MinecraftServer) {
            if (server.ticks.rem(SNAP_TICK_FREQUENCY) == 0) {
                server.playerManager.playerList.forEach(FlashbackController::snapNow)
            }
        }
    }

    private fun snapNow(player: ServerPlayerEntity) = player.send(SNAP_PACKET)

    private fun replayNow(player: ServerPlayerEntity) = player.send(REPLAY_PACKET)
}
