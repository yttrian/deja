package deja

import com.mojang.brigadier.context.CommandContext
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object SnapController {
    val SNAP_PACKET = Identifier("deja", "snap")
    val REPLAY_PACKET = Identifier("snap", "replay")

    fun snapNow(context: CommandContext<ServerCommandSource>) = context.source.player.send(SNAP_PACKET)

    fun replayNow(context: CommandContext<ServerCommandSource>) = context.source.player.send(REPLAY_PACKET)

    private fun ServerPlayerEntity.send(identifier: Identifier) =
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(this, identifier, PacketByteBuf(Unpooled.buffer()))

}