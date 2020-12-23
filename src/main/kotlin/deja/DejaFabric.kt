package deja

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

/**
 * Entry point for the Deja mod
 */
object DejaFabric : ModInitializer, CommandRegistrar {
    /**
     * On server mod initialize
     */
    override fun onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(TimeloopController.ServerTick)

        registerCommands("deja") {
            does { respond("DejaFabric") }

            add(TimeloopController)
        }
    }
}
