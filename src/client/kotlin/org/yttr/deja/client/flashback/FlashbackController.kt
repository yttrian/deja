package org.yttr.deja.client.flashback

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotUtils
import org.yttr.deja.Controller
import java.time.Duration

/**
 * Flashback controller
 */
object FlashbackController : Controller("flashback") {
    private const val SNAP_FREQUENCY_SECONDS = 5L
    private val SNAP_FREQUENCY_TICKS = Duration.ofSeconds(SNAP_FREQUENCY_SECONDS).toTicks()

    private val client = MinecraftClient.getInstance()
    private val snaps = mutableListOf<NativeImage>()
    private var snapTickCountdown = SNAP_FREQUENCY_TICKS

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

            if (--snapTickCountdown < 0) {
                snap()
            }
        }
    }

    /**
     * Begin a flashback
     */
    fun go() = client.openScreen(FlashbackPlayer(snaps))

    private fun snap() {
        snapTickCountdown = SNAP_FREQUENCY_TICKS
        RenderSystem.recordRenderCall {
            val window = client.window
            val snap = ScreenshotUtils.takeScreenshot(
                window.framebufferWidth,
                window.framebufferHeight,
                client.framebuffer
            )
            snaps.add(snap)
        }
    }
}
