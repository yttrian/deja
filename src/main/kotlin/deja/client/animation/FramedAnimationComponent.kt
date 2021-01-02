package deja.client.animation

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11C

/**
 * An animatable component that views its timeline as starting at 0 even if part of a larger timeline.
 */
abstract class FramedAnimationComponent(private val screen: AnimationScreen, private var startTime: Float) {
    constructor(screen: AnimationScreen, startTime: Int) : this(screen, startTime.toFloat())

    /**
     * Screen width
     */
    protected val width
        get() = screen.width

    /**
     * Screen height
     */
    protected val height
        get() = screen.height

    /**
     * Close the screen
     */
    protected fun close() = screen.close()

    /**
     * Linear interpolate between two values over time
     */
    protected fun linearInterpolate(time: Float, duration: Int, min: Float, max: Float) =
        (max - min) / duration * time + min

    /**
     * Rebase the start time to a new screen based time, default value is current screen time
     */
    protected fun rebase(time: Float = screen.time) {
        startTime = time
    }

    /**
     * Draw an image treating the center of the screen as (0, 0) position and the center of the image as the (x, y)
     */
    @Suppress("LongParameterList")
    private fun drawImage(
        matrices: MatrixStack,
        texture: Identifier,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        u0: Float = 0f,
        u1: Float = 1f,
        v0: Float = 0f,
        v1: Float = 1f
    ) {
        TEXTURE_MANAGER.bindTexture(texture)

        val x0 = this.width / 2f - width / 2f + x
        val y0 = this.height / 2f - height / 2f + y
        val x1 = x0 + width
        val y1 = y0 + height

        val bufferBuilder = Tessellator.getInstance().buffer
        val matrix = matrices.peek().model
        bufferBuilder.begin(GL11C.GL_QUADS, VertexFormats.POSITION_TEXTURE)
        bufferBuilder.vertex(matrix, x0, y1, 0f).texture(u0, v1).next()
        bufferBuilder.vertex(matrix, x1, y1, 0f).texture(u1, v1).next()
        bufferBuilder.vertex(matrix, x1, y0, 0f).texture(u1, v0).next()
        bufferBuilder.vertex(matrix, x0, y0, 0f).texture(u0, v0).next()
        bufferBuilder.end()
        BufferRenderer.draw(bufferBuilder)
    }

    /**
     * Draw a sprite at the specified x and y coordinates at the defined scale (1 being normal size)
     */
    protected fun drawSprite(matrices: MatrixStack, sprite: Sprite, x: Float, y: Float, scale: Float) =
        drawImage(
            matrices,
            sprite.atlas.id,
            x,
            y,
            sprite.width * scale,
            sprite.height * scale,
            sprite.minU,
            sprite.maxU,
            sprite.minV,
            sprite.maxV
        )

    /**
     * Draw an image centered on the screen
     */
    protected fun drawCenteredImage(matrices: MatrixStack, texture: Identifier, width: Float, height: Float) =
        drawImage(matrices, texture, 0f, 0f, width, height)

    /**
     * Render the current component using the time provided by the Screen at initialization
     */
    fun render(matrices: MatrixStack) {
        val time = screen.time - startTime

        if (time >= 0) {
            render(matrices, time)
        }
    }

    /**
     * Render loop
     */
    protected abstract fun render(matrices: MatrixStack, time: Float)

    companion object {
        private val TEXTURE_MANAGER = MinecraftClient.getInstance().textureManager
    }
}
