package deja.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import java.awt.Color
import kotlin.math.floor
import kotlin.math.pow

class Flashback(rawMemories: List<NativeImage>) : Screen(LiteralText("deja.flashback")) {
    private val textureManager = MinecraftClient.getInstance().textureManager

    private val memories = rawMemories.asReversed().map { it.toTexture() }
    private val totalMemories = memories.size
    private val end = (totalMemories.coerceAtMost(MEMORY_GOAL) / MEMORY_GOAL.toFloat()) * MAX_END_TIME
    private var time = 0f

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        time += delta

        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)

        val memory = memories.getOrNull(currentMemory()) ?: return onClose()

        textureManager.bindTexture(memory)

        fun Float.step(n: Int): Int = (floor(this / n.toFloat()) * n).toInt()

        val zoom = ((1 - BASE_ZOOM) / end) * time + BASE_ZOOM
        val memoryWidth = (width * zoom).step(2)
        val memoryHeight = (height * zoom).step(2)
        val memoryX = width / 2 - memoryWidth / 2
        val memoryY = height / 2 - memoryHeight / 2

        drawTexture(
            matrices,
            memoryX,
            memoryY,
            0f,
            0f,
            memoryWidth,
            memoryHeight,
            memoryWidth,
            memoryHeight,
        )
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