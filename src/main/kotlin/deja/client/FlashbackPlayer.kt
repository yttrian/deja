package deja.client

import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import java.awt.Color
import kotlin.math.pow

/**
 * Screen for playing flashbacks
 */
class FlashbackPlayer(rawMemories: MutableList<NativeImage>) : AnimationScreen(LiteralText("deja.flashback")) {
    private val memories = rawMemories.asReversed().map { it.toTexture("memory") }.also { rawMemories.clear() }
    private val totalMemories = memories.size
    private val memoryDuration =
        ((totalMemories.coerceAtMost(MEMORY_GOAL) / MEMORY_GOAL.toFloat()) * MEMORY_TIME_MAX)
            .toInt().coerceAtLeast(MEMORY_TIME_MIN)

    /**
     * Render loop
     */
    override fun render(matrices: MatrixStack) {
        fun drawLastMemory() = memories.lastOrNull()?.let {
            drawCenteredImage(matrices, it, width.toFloat(), height.toFloat())
        }

        if (closing) {
            drawLastMemory()
            return
        }

        if (time > MEMORY_TIME_START) {
            drawMemory(matrices)
        }

        if (closing) {
            drawLastMemory()
        } else {
            drawMask(matrices)
        }
    }

    private fun drawMask(matrices: MatrixStack) {
        val z = linearInterpolate(0, MASK_TIME, MASK_Z_MIN, MASK_Z_MAX)

        // https://www.desmos.com/calculator/xijpj8s4vs
        val dim = height / z.coerceAtLeast(0f)

        drawCenteredImage(matrices, MASK_TEXTURE, dim, dim)
        if (time < FADE_TIME) {
            drawFade(matrices)
        }
    }

    private fun drawMemory(matrices: MatrixStack) {
        val currentMemory = (((time - MEMORY_TIME_START) / memoryDuration).pow(3) * totalMemories).toInt()
        val memory = memories.getOrNull(currentMemory) ?: return close()

        val scale = linearInterpolate(MEMORY_TIME_START, memoryDuration, MEMORY_SCALE_MIN, MEMORY_SCALE_MAX)

        // https://www.desmos.com/calculator/s9dxn0mgxl
        val memoryWidth = width * scale
        val memoryHeight = height * scale

        drawCenteredImage(matrices, memory, memoryWidth, memoryHeight)
    }

    private fun drawFade(matrices: MatrixStack) {
        val alpha = linearInterpolate(0, FADE_TIME, 1f, 0f).coerceAtLeast(0f)
        val fade = Color(0f, 0f, 0f, alpha)
        DrawableHelper.fill(matrices, 0, 0, width, height, fade.rgb)
    }

    // https://www.desmos.com/calculator/pa7q4vcnho
    private fun linearInterpolate(startTime: Int, duration: Int, min: Float, max: Float) =
        (max - min) / duration * (time - startTime) + min

    /**
     * Render a solid black background
     */
    override fun renderBackground(matrices: MatrixStack) {
        DrawableHelper.fill(matrices, 0, 0, width, height, Color.BLACK.rgb)
    }

    /**
     * Cleanup on close
     */
    override fun onClose() {
        memories.forEach { it.destroy() }
        super.onClose()
    }

    companion object {
        private const val MEMORY_GOAL: Int = 80
        private const val MASK_TIME: Int = 6 * TICKS_PER_SECOND
        private const val FADE_TIME: Int = 4 * TICKS_PER_SECOND
        private const val MEMORY_TIME_START: Int = (MASK_TIME * 0.9f).toInt()
        private const val MEMORY_TIME_MIN: Int = 4 * TICKS_PER_SECOND
        private const val MEMORY_TIME_MAX: Int = 27 * TICKS_PER_SECOND
        private const val MASK_Z_MIN: Float = 8f
        private const val MASK_Z_MAX: Float = 0 - 1f
        private const val MEMORY_SCALE_MIN: Float = 0.05f
        private const val MEMORY_SCALE_MAX: Float = 1f
        private val MASK_TEXTURE = Identifier("deja", "textures/gui/mask.png")
    }
}
