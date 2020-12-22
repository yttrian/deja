package deja

import net.fabricmc.api.ModInitializer

object DejaFabric : ModInitializer, CommandRegistrar {
    override fun onInitialize() {
        registerCommands("deja") {
            does { respond("DejaFabric") }

            command("snap") {
                does(SnapController::snapNow)
            }

            command("replay") {
                does(SnapController::replayNow)
            }
        }
    }
}
