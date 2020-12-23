package deja.client

import com.mojang.blaze3d.systems.RenderSystem
import deja.Controller
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotUtils
import java.time.Duration

/**
 * Flashback controller
 */
object FlashbackController : Controller("flashback") {
    private val SNAP_TICK_FREQUENCY = Duration.ofSeconds(5).toTicks()

    private val client = MinecraftClient.getInstance()
    private val snaps = mutableListOf<NativeImage>()
    private var lastSnap = 0
    private var ticks = 0

    /**
     * Flashback client tick handler
     */
    object ClientTick : ClientTickEvents.StartTick {
        /**
         * On tick
         */
        override fun onStartTick(client: MinecraftClient) {
            // do not snap when not in world (like title screen, but allow menus when it world)
            // also do not snap the flashback, that'd be weird
            if (client.world == null || client.currentScreen is FlashbackPlayer) return

            ticks++
            if (ticks - lastSnap > SNAP_TICK_FREQUENCY) {
                snap()
            }
        }
    }

    /**
     * Begin a flashback
     */
    fun go() = client.openScreen(FlashbackPlayer(snaps))

    private fun snap() = RenderSystem.recordRenderCall {
        lastSnap = ticks
        val window = client.window
        val snap = ScreenshotUtils.takeScreenshot(
            window.framebufferWidth,
            window.framebufferHeight,
            PacketRegistrar.Office.client.framebuffer
        )
        snaps.add(snap)
    }
}
