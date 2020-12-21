package deja

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

interface CommandRegistrar{
    fun registerCommands(base: String, dsl: LiteralArgumentBuilder<ServerCommandSource>.() -> Unit) {
        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(CommandManager.literal(base).also(dsl))
        }
    }

    fun LiteralArgumentBuilder<ServerCommandSource>.command(
        command: String,
        sub: LiteralArgumentBuilder<ServerCommandSource>.() -> Unit
    ): LiteralArgumentBuilder<ServerCommandSource> =
        this.then(CommandManager.literal(command).also { it.sub() })

    fun LiteralArgumentBuilder<ServerCommandSource>.does(
        action: CommandContext<ServerCommandSource>.() -> Unit
    ): LiteralArgumentBuilder<ServerCommandSource> = this.executes {
        action(it)
        Command.SINGLE_SUCCESS
    }

    fun CommandContext<ServerCommandSource>.respond(message: String): Unit =
        this.source.sendFeedback(LiteralText(message), false)
}