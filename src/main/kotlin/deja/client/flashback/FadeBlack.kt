package deja.client.flashback

import deja.client.animation.AnimationScreen
import deja.client.animation.FramedAnimationComponent
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import java.awt.Color

/**
 * Fade to or from black
 */
class FadeBlack(
    screen: AnimationScreen,
    startTime: Int,
    private val duration: Int,
    fromBlack: Boolean = true
) : FramedAnimationComponent(screen, startTime) {
    private val min = if (fromBlack) 1f else 0f
    private val max = if (fromBlack) 0f else 1f

    override fun render(matrices: MatrixStack, time: Float) {
        val alpha = linearInterpolate(time, duration, min, max)
        val fade = Color(0f, 0f, 0f, alpha)
        DrawableHelper.fill(matrices, 0, 0, width, height, fade.rgb)
    }
}
