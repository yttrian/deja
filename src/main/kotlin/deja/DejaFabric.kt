package deja

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object DejaFabric : ModInitializer, CommandRegistrar {
    override fun onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(TimeloopController.ServerTick)

        registerCommands("deja") {
            does { respond("DejaFabric") }

            add(TimeloopController)
        }
    }
}
