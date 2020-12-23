package deja.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11C
import java.awt.Color
import kotlin.math.pow

/**
 * Screen for playing flashbacks
 */
class FlashbackPlayer(rawMemories: MutableList<NativeImage>) : Screen(LiteralText("deja.flashback")) {
    private val textureManager = MinecraftClient.getInstance().textureManager

    private val memories = rawMemories.asReversed().map { it.toTexture() }.also { rawMemories.clear() }
    private val totalMemories = memories.size
    private val memoryDuration =
        ((totalMemories.coerceAtMost(MEMORY_GOAL) / MEMORY_GOAL.toFloat()) * MEMORY_TIME_MAX)
            .toInt().coerceAtLeast(MEMORY_TIME_MIN)
    private var time = 0f
    private var closing = false

    /**
     * Render loop
     */
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        fun drawLastMemory() = drawCenteredImage(matrices, memories.last(), width.toFloat(), height.toFloat())

        if (closing) {
            drawLastMemory()
            return
        }

        time += delta

        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)

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
        val zoom =
            ((MASK_ZOOM_MAX - MASK_ZOOM_MIN) / MASK_TIME) * (time.pow(4) / MASK_TIME.pow(3)) + MASK_ZOOM_MIN

        val maskHeight = height * zoom

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

    private fun drawCenteredImage(
        matrices: MatrixStack,
        texture: Identifier,
        width: Float,
        height: Float
    ) {
        textureManager.bindTexture(texture)

        val x0 = this.width / 2f - width / 2f
        val y0 = this.height / 2f - height / 2f
        val x1 = x0 + width
        val y1 = y0 + height

        val bufferBuilder = Tessellator.getInstance().buffer
        val matrix = matrices.peek().model
        bufferBuilder.begin(GL11C.GL_QUADS, VertexFormats.POSITION_TEXTURE)
        bufferBuilder.vertex(matrix, x0, y1, 0f).texture(0f, 1f).next()
        bufferBuilder.vertex(matrix, x1, y1, 0f).texture(1f, 1f).next()
        bufferBuilder.vertex(matrix, x1, y0, 0f).texture(1f, 0f).next()
        bufferBuilder.vertex(matrix, x0, y0, 0f).texture(0f, 0f).next()
        bufferBuilder.end()
        BufferRenderer.draw(bufferBuilder)
    }

    /**
     * Render a solid black background
     */
    override fun renderBackground(matrices: MatrixStack?) {
        DrawableHelper.fill(matrices, 0, 0, width, height, Color.BLACK.rgb)
    }

    private fun NativeImage.toTexture(): Identifier =
        textureManager.registerDynamicTexture("memory", NativeImageBackedTexture(this))

    private fun Identifier.destroy(): Unit = textureManager.destroyTexture(this)

    private fun close() {
        closing = true
        onClose()
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
        private const val TICKS_PER_SECOND: Int = 20
        private const val MEMORY_GOAL: Int = 80
        private const val MASK_TIME: Int = 9 * TICKS_PER_SECOND
        private const val MEMORY_TIME_START: Int = (MASK_TIME * 0.40f).toInt()
        private const val MEMORY_TIME_MIN: Int = MASK_TIME + MEMORY_TIME_START * 2
        private const val MEMORY_TIME_MAX: Int = 27 * TICKS_PER_SECOND
        private const val MASK_ZOOM_MIN: Float = 0.1f
        private const val MASK_ZOOM_MAX: Float = 20.0f
        private const val MEMORY_ZOOM_MIN: Float = 0.05f
        private const val MEMORY_ZOOM_MAX: Float = 1f
        private val MASK_TEXTURE = Identifier("deja", "textures/gui/mask.png")
    }
}
