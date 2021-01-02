package deja.client.animation

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

/**
 * Screen used for animations
 */
abstract class AnimationScreen(title: Text) : Screen(title) {
    private val textureManager = MinecraftClient.getInstance().textureManager

    /**
     * Time elapsed since opening (in ticks)
     */
    var time = 0f
        protected set

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
    fun close() = onClose()

    companion object {
        /**
         * Standard number of ticks per second
         */
        const val TICKS_PER_SECOND: Int = 20
    }
}
