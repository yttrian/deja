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
        fun drawLastMemory() = drawCenteredImage(matrices, memories.last(), width.toFloat(), height.toFloat())

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
        // https://www.desmos.com/calculator/00tmrlguuw
        val zoom = (MASK_ZOOM_MAX - MASK_ZOOM_MIN) / MASK_TIME * time + MASK_ZOOM_MIN

        val maskHeight = height * 1 / zoom.coerceAtLeast(0f)

        drawCenteredImage(matrices, MASK_TEXTURE, maskHeight, maskHeight)
    }

    private fun drawMemory(matrices: MatrixStack) {
        val currentMemory = (((time - MEMORY_TIME_START) / memoryDuration).pow(3) * totalMemories).toInt()
        val memory = memories.getOrNull(currentMemory) ?: return close()

        // https://www.desmos.com/calculator/pa7q4vcnho
        val zoom = (MEMORY_ZOOM_MAX - MEMORY_ZOOM_MIN) / memoryDuration * (time - MEMORY_TIME_START) + MEMORY_ZOOM_MIN

        // https://www.desmos.com/calculator/s9dxn0mgxl
        val memoryWidth = width * zoom
        val memoryHeight = height * zoom

        drawCenteredImage(matrices, memory, memoryWidth, memoryHeight)
    }

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

    private fun Int.pow(n: Int) = this.toFloat().pow(n)

    companion object {
        private const val MEMORY_GOAL: Int = 80
        private const val MASK_TIME: Int = 6 * TICKS_PER_SECOND
        private const val MEMORY_TIME_START: Int = (MASK_TIME * 0.9f).toInt()
        private const val MEMORY_TIME_MIN: Int = MASK_TIME + MEMORY_TIME_START
        private const val MEMORY_TIME_MAX: Int = 27 * TICKS_PER_SECOND
        private const val MASK_ZOOM_MIN: Float = 15f
        private const val MASK_ZOOM_MAX: Float = 0f
        private const val MEMORY_ZOOM_MIN: Float = 0.05f
        private const val MEMORY_ZOOM_MAX: Float = 1f
        private val MASK_TEXTURE = Identifier("deja", "textures/gui/mask.png")
    }
}
