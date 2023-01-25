package org.yttr.deja.client.flashback

import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import org.yttr.deja.client.animation.AnimationScreen
import org.yttr.deja.client.animation.FramedAnimationComponent
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
        val alpha = linearInterpolate(time, duration, min, max).coerceIn(0f, 1f)
        if (alpha > 0) {
            val fade = Color(0f, 0f, 0f, alpha)
            DrawableHelper.fill(matrices, 0, 0, width, height, fade.rgb)
        }
    }
}
