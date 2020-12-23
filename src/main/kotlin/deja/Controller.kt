package deja

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.time.Duration

/**
 * A controller.
 * Capable of registering commands and contains several helpful extension functions.
 */
abstract class Controller(
    /**
     * Friendly name to use for commands, must not contain spaces
     */
    friendlyName: String
) : CommandRegistrar {
    init {
        assert(friendlyName.count(Char::isWhitespace) == 0)
    }

    /**
     * Base command for commands the controller registers
     */
    val baseCommand: LiteralArgumentBuilder<ServerCommandSource> =
        CommandManager.literal(friendlyName)

    /**
     * Register a command under the baseCommand
     */
    protected fun command(
        command: String,
        sub: LiteralArgumentBuilder<ServerCommandSource>.() -> Unit
    ): LiteralArgumentBuilder<ServerCommandSource> =
        baseCommand.then(CommandManager.literal(command).also { it.sub() })

    /**
     * Send a pack to a player
     */
    protected fun ServerPlayerEntity.send(identifier: Identifier) =
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(this, identifier, PacketByteBuf(Unpooled.buffer()))

    /**
     * Convert time to tick assuming 20 ticks per second
     */
    protected fun Duration.toTicks(): Long = this.seconds * TICKS_PER_SECOND

    companion object {
        private const val TICKS_PER_SECOND = 20
    }
}
