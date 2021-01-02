package deja.client.flashback

import deja.client.animation.Animation.TICKS_PER_SECOND
import deja.client.animation.AnimationScreen
import deja.client.animation.FramedAnimationComponent
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import kotlin.math.pow

/**
 * A display of memories that seemingly approaches the camera
 */
class Memories(screen: AnimationScreen, startTime: Int, private val memories: List<Identifier>) :
    FramedAnimationComponent(screen, startTime) {
    private val totalMemories = memories.size
    private val duration =
        ((totalMemories.coerceAtMost(MEMORY_GOAL) / MEMORY_GOAL.toFloat()) * MEMORY_TIME_MAX)
            .toInt().coerceAtLeast(MEMORY_TIME_MIN)

    override fun render(matrices: MatrixStack, time: Float) {
        val currentMemory = ((time / duration).pow(3) * totalMemories).toInt()
        val memory = memories.getOrNull(currentMemory)

        if (memory == null) {
            memories.lastOrNull()?.let {
                drawCenteredImage(matrices, it, width.toFloat(), height.toFloat())
            }
            return close()
        }

        val z = linearInterpolate(time, duration, MEMORY_Z_MIN, MEMORY_Z_MAX)

        // https://www.desmos.com/calculator/s9dxn0mgxl
        val memoryWidth = width / z
        val memoryHeight = height / z

        drawCenteredImage(matrices, memory, memoryWidth, memoryHeight)
    }

    companion object {
        private const val MEMORY_GOAL: Int = 80
        private const val MEMORY_TIME_MIN: Int = 4 * TICKS_PER_SECOND
        private const val MEMORY_TIME_MAX: Int = 27 * TICKS_PER_SECOND
        private const val MEMORY_Z_MIN: Float = 15f
        private const val MEMORY_Z_MAX: Float = 1f
    }
}
