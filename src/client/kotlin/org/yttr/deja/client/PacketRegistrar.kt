package org.yttr.deja.client

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.Window
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier

/**
 * A DSL for registering packet listeners
 */
interface PacketRegistrar {
    /**
     * Entrypoint for registering packets
     */
    fun registerPackets(dsl: Office.() -> Unit) {
        Office.dsl()
    }

    /**
     * Wrap the registry to provide shorthand access to useful values
     */
    object Office : ClientSidePacketRegistry by ClientSidePacketRegistry.INSTANCE {
        /**
         * The Minecraft client
         */
        val client: MinecraftClient by lazy { MinecraftClient.getInstance() }
    }

    /**
     * Register an action for packets that use empty messages
     */
    fun Office.action(identifier: Identifier, action: Action.() -> Unit) {
        register(identifier) { context, _ ->
            Action(context).also(action)
        }
    }

    /**
     * Wrap the packet context to provide shorthand access to useful values
     */
    class Action(packetContext: PacketContext) : PacketContext by packetContext {
        /**
         * The Minecraft client window
         */
        val window: Window get() = Office.client.window
    }

    /**
     * Display a message to the user
     */
    fun Action.message(message: String, actionBar: Boolean = false) =
        player.sendMessage(LiteralText(message), actionBar)
}
