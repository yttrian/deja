package deja

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

abstract class Controller(
    /**
     * Friendly name to use for commands, must not contain spaces
     */
    friendlyName: String
) : CommandRegistrar {
    init {
        assert(friendlyName.count(Char::isWhitespace) == 0)
    }

    val baseCommand: LiteralArgumentBuilder<ServerCommandSource> =
        CommandManager.literal(friendlyName)

    protected fun command(
        command: String,
        sub: LiteralArgumentBuilder<ServerCommandSource>.() -> Unit
    ): LiteralArgumentBuilder<ServerCommandSource> =
        baseCommand.then(CommandManager.literal(command).also { it.sub() })

    protected fun ServerPlayerEntity.send(identifier: Identifier) =
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(this, identifier, PacketByteBuf(Unpooled.buffer()))
}
