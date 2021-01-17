package deja.client.flashback

import deja.client.animation.Animation.TICKS_PER_SECOND
import deja.client.animation.AnimationScreen
import deja.client.animation.FramedAnimationComponent
import deja.client.animation.Spread
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.registry.Registry
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.random.Random

/**
 * Trail of glyphs seen in the "memory tunnel"
 */
class GlyphTrail(private val screen: AnimationScreen, spread: Spread) :
    FramedAnimationComponent(screen, EARLIEST_START_TIME * spread.fraction) {
    private val trail = List(TRAIL_LENGTH) { Glyph(this, it) }

    //    private val miniSpread = spread.split()
    private val posX by lazy { choosePosition(width, spread.isUpper) }
    private val posY by lazy { choosePosition(height, spread.isLower) }
    private val shuffledSprites = sprites.shuffled().asWrap()

    private fun choosePosition(dimension: Int, full: Boolean): Int {
        val bounds = dimension / 2
        return if (full) {
            Random.nextInt(-bounds, bounds)
        } else {
            bounds * if (Random.nextBoolean()) 1 else -1
        }
    }

    /**
     * Render the glyph trail
     */
    override fun render(matrices: MatrixStack, time: Float) {
        // Don't render "outside" the mask
        val safetyFactor = linearInterpolate(screen.time, FlashbackPlayer.MASK_TIME, Mask.MASK_Z_MIN, Mask.MASK_Z_MAX)
            .coerceAtLeast(0f)
        val safety = Safety(width / 2 / safetyFactor * SAFETY_LAG, height / 2 / safetyFactor * SAFETY_LAG)

        trail.forEach {
            it.render(matrices, time, safety)
        }

        if (time > TRAVEL_TIME) {
            rebase(screen.time + (TRAVEL_TIME - time))
        }
    }

    private data class Safety(val safeX: Float, val safeY: Float) {
        fun isSafe(x: Float, y: Float) = x.absoluteValue <= safeX && y.absoluteValue <= safeY
    }

    private class Glyph(val glyphTrail: GlyphTrail, val position: Int) {
        // https://www.desmos.com/calculator/runxxwoydu
        private fun chooseSprite(time: Float) =
            glyphTrail.shuffledSprites[ceil((time / FREQUENCY + position + 1) / TRAIL_LENGTH) + position]

        fun render(matrices: MatrixStack, time: Float, safety: Safety) {
            val z = glyphTrail
                .linearInterpolate(time, TRAVEL_TIME, Z_MIN + position * Z_SPACING, Z_MAX)
                .coerceAtLeast(0f)
            val x = glyphTrail.posX / z
            val y = glyphTrail.posY / z

            if (safety.isSafe(x, y)) {
                val sprite = chooseSprite(time)
                glyphTrail.drawSprite(matrices, sprite, x, y, 2 / z)
            }
        }
    }

    private class WrapList<T>(val list: List<T>) : List<T> by list {
        override operator fun get(index: Int): T = list[index.rem(this.size)]
        operator fun get(index: Float): T = get(index.toInt())
        override fun toString() = list.toString()
    }

    private fun <T> List<T>.asWrap() = WrapList(this)

    companion object {
        private const val TRAIL_LENGTH: Int = 12
        private const val Z_MIN: Float = 12f
        private const val Z_MAX: Float = 0f
        private const val Z_SPACING: Float = 2f
        private const val FREQUENCY: Float = 1.2f * TICKS_PER_SECOND / TRAIL_LENGTH
        private const val TRAVEL_TIME: Int = 10 * TICKS_PER_SECOND
        private const val EARLIEST_START_TIME: Float = TRAVEL_TIME * -1f
        private const val SAFETY_LAG: Float = 0.3f

        private val sprites = MinecraftClient.getInstance().let { client ->
            val particleManagerAccessor = client.particleManager as ParticleManagerAccessor
            val enchantParticleType = Registry.PARTICLE_TYPE.getId(ParticleTypes.ENCHANT)
            val spriteAwareFactories = particleManagerAccessor.spriteAwareFactories
            val enchantParticleSpriteProviderAccessor =
                spriteAwareFactories[enchantParticleType] as ParticleManagerAccessor.SimpleSpriteProviderAccessor
            enchantParticleSpriteProviderAccessor.sprites.toList()
        }
    }
}
