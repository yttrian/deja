package deja.client

import com.mojang.blaze3d.systems.RenderSystem
import deja.Controller
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotUtils
import java.time.Duration

@Environment(EnvType.CLIENT)
object FlashbackController : Controller("flashback") {
    private val SNAP_TICK_FREQUENCY = Duration.ofSeconds(5).toTicks()

    private val client = MinecraftClient.getInstance()
    private val snaps = mutableListOf<NativeImage>()
    private var ticks = 0

    object ClientTick : ClientTickEvents.StartTick {
        override fun onStartTick(client: MinecraftClient) {
            ticks++
            if (ticks.rem(SNAP_TICK_FREQUENCY) == 0L) {
                snap()
            }
        }
    }

    fun go() = client.openScreen(FlashbackPlayer(snaps))

    fun snap() = RenderSystem.recordRenderCall {
        val window = client.window
        val snap = ScreenshotUtils.takeScreenshot(
            window.framebufferWidth,
            window.framebufferHeight,
            PacketRegistrar.Office.client.framebuffer
        )
        snaps.add(snap)
    }
}
