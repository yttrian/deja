package deja.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11C

/**
 * Screen used for animations
 */
abstract class AnimationScreen(title: Text) : Screen(title) {
    private val textureManager = MinecraftClient.getInstance().textureManager

    /**
     * Time elapsed since opening (in ticks)
     */
    protected var time = 0f

    /**
     * Whether or not the the screen is currently closing (close called)
     */
    protected var closing = false

    /**
     * Render loop, with time tracking
     */
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        time += delta
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        render(matrices)
    }

    /**
     * Animation loop
     */
    abstract fun render(matrices: MatrixStack)

    /**
     * Draw an image centered on the screen
     */
    protected fun drawCenteredImage(
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
     * Convert NativeImage to texture and get Identifier
     */
    protected fun NativeImage.toTexture(prefix: String): Identifier =
        textureManager.registerDynamicTexture(prefix, NativeImageBackedTexture(this))

    /**
     * Destroy texture
     */
    protected fun Identifier.destroy(): Unit = textureManager.destroyTexture(this)

    /**
     * Do not close on Esc
     */
    override fun shouldCloseOnEsc(): Boolean = false

    /**
     * Close the screen
     */
    protected fun close() {
        closing = true
        onClose()
    }

    companion object {
        /**
         * Standard number of ticks per second
         */
        const val TICKS_PER_SECOND: Int = 20
    }
}
