package deja.client.flashback

import deja.client.animation.AnimationScreen
import deja.client.animation.FramedAnimationComponent
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

/**
 * The Minecraft-y equivalent of the memory mask that the camera flies through
 */
class Mask(screen: AnimationScreen, startTime: Int, private val duration: Int) :
    FramedAnimationComponent(screen, startTime) {
    override fun render(matrices: MatrixStack, time: Float) {
        val z = linearInterpolate(time, duration, MASK_Z_MIN, MASK_Z_MAX)

        // https://www.desmos.com/calculator/xijpj8s4vs
        val dim = height / z.coerceAtLeast(0f)

        drawCenteredImage(matrices, MASK_TEXTURE, dim, dim)
    }

    companion object {
        private val MASK_TEXTURE = Identifier("deja", "textures/gui/mask.png")

        /**
         * Starting Z position of the mask
         */
        const val MASK_Z_MIN: Float = 5f

        /**
         * Ending Z position of the mask
         */
        const val MASK_Z_MAX: Float = 0 - 1f
    }
}
