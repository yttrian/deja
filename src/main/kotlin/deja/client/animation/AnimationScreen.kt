package deja.client.animation

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.apache.logging.log4j.util.PerformanceSensitive
import org.lwjgl.glfw.GLFW

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

    private var cursorVisible: Boolean
        get() = GLFW.glfwGetInputMode(client!!.window.handle, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL
        set(visible) = GLFW.glfwSetInputMode(
            client!!.window.handle,
            GLFW.GLFW_CURSOR,
            if (visible) GLFW.GLFW_CURSOR_NORMAL else GLFW.GLFW_CURSOR_HIDDEN
        )

    /**
     * Render loop, with time tracking
     */
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        time += delta
        renderBackground(matrices)
        render(matrices)
    }

    /**
     * Animation loop
     */
    @PerformanceSensitive("Minecraft Render Loop")
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
     * Hide the cursor on screen initialization (client should be set and not null by this point)
     */
    override fun init() {
        cursorVisible = false
    }

    /**
     * Do not close on Esc
     */
    override fun shouldCloseOnEsc(): Boolean = false

    /**
     * Close the screen
     */
    fun close() = onClose()
}
