package deja.client

import deja.TimeloopController
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

@Environment(EnvType.CLIENT)
object DejaFabricClient : ClientModInitializer, PacketRegistrar {
    override fun onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(FlashbackController.ClientTick)

        registerPackets {
            action(TimeloopController.REPLAY_PACKET) {
                FlashbackController.go()
            }
        }
    }
}
