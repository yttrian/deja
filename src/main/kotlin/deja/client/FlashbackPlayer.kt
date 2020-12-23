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

class FlashbackPlayer(rawMemories: MutableList<NativeImage>) : Screen(LiteralText("deja.flashback")) {
    private val textureManager = MinecraftClient.getInstance().textureManager

    private val memories = rawMemories.asReversed().map { it.toTexture() }.also { rawMemories.clear() }
    private val totalMemories = memories.size
    private val end = (totalMemories.coerceAtMost(MEMORY_GOAL) / MEMORY_GOAL.toFloat()) * MAX_END_TIME
    private var time = 0f

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        time += delta

        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)

        val memory = memories.getOrNull(currentMemory()) ?: return onClose()

        textureManager.bindTexture(memory)

        val zoom = (1 - BASE_ZOOM) / end * time + BASE_ZOOM
        val memoryWidth = width * zoom
        val memoryHeight = height * zoom
        val memoryX = width / 2f - memoryWidth / 2f
        val memoryY = height / 2f - memoryHeight / 2f

        drawMemory(
            matrices,
            memoryX,
            memoryY,
            memoryWidth,
            memoryHeight
        )
    }

    private fun drawMemory(
        matrices: MatrixStack,
        x0: Float,
        y0: Float,
        width: Float,
        height: Float
    ) {
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

    private fun currentMemory(): Int = ((time / end).pow(3) * totalMemories).toInt()

    override fun renderBackground(matrices: MatrixStack?) {
        DrawableHelper.fill(matrices, 0, 0, width, height, Color.BLACK.rgb)
    }

    private fun NativeImage.toTexture(): Identifier =
        textureManager.registerDynamicTexture("memory", NativeImageBackedTexture(this))

    private fun Identifier.destroy(): Unit = textureManager.destroyTexture(this)

    override fun onClose() {
        memories.forEach { it.destroy() }
        super.onClose()
    }

    companion object {
        private const val TICKS_PER_SECOND: Float = 20f
        private const val MEMORY_GOAL: Int = 80
        private const val MAX_END_TIME: Float = 30 * TICKS_PER_SECOND
        private const val BASE_ZOOM: Float = 0.3f
    }
}
