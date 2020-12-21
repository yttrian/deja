package deja

import net.fabricmc.api.ModInitializer

object DejaFabric : ModInitializer, CommandRegistrar {
    override fun onInitialize() {
        registerCommands("deja") {
            command("bar") {
                does {
                    respond("bar!")
                }
            }

            command("baz") {
                does {
                    respond("baz!")
                }
            }
        }
    }
}
