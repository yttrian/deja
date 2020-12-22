package deja.client

import com.mojang.blaze3d.systems.RenderSystem
import deja.FlashbackController
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotUtils

@Environment(EnvType.CLIENT)
object DejaFabricClient : ClientModInitializer, PacketRegistrar {
    private val snaps = mutableListOf<NativeImage>()

    override fun onInitializeClient() {
        registerPackets {
            action(FlashbackController.SNAP_PACKET) {
                RenderSystem.recordRenderCall {
                    val snap = ScreenshotUtils.takeScreenshot(
                        window.framebufferWidth,
                        window.framebufferHeight,
                        client.framebuffer
                    )
                    snaps.add(snap)
                }
                message("SNAP ${snaps.size}!", true)
            }

            action(FlashbackController.REPLAY_PACKET) {
                client.openScreen(Flashback(snaps))
                message("REPLAY ${snaps.size}!", true)
            }
        }
    }
}
