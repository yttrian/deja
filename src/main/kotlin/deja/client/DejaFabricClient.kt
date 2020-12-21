package deja.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object DejaFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        println("CLIENT!")
    }
}
