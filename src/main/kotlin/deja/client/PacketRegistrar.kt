package deja.client

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.Window
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier

interface PacketRegistrar {
    object Office : ClientSidePacketRegistry by ClientSidePacketRegistry.INSTANCE {
        val client: MinecraftClient by lazy { MinecraftClient.getInstance() }
    }

    fun Office.action(identifier: Identifier, action: Action.() -> Unit) {
        register(identifier) { context, _ ->
            Action(context).also(action)
        }
    }

    class Action(packetContext: PacketContext) : PacketContext by packetContext {
        val window: Window get() = Office.client.window
    }

    fun Action.message(message: String, actionBar: Boolean = false) =
        player.sendMessage(LiteralText(message), actionBar)

    fun registerPackets(dsl: Office.() -> Unit) {
        Office.dsl()
    }
}