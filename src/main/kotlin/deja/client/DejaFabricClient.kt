package deja.client

import deja.TimeloopController
import deja.client.flashback.FlashbackController
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

/**
 * Entry point for the Deja client mod
 */
@Environment(EnvType.CLIENT)
object DejaFabricClient : ClientModInitializer, PacketRegistrar {
    /**
     * On client mod initialize
     */
    override fun onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(FlashbackController.ClientTick)

        registerPackets {
            action(TimeloopController.REPLAY_PACKET) {
                FlashbackController.go()
            }
        }
    }
}
