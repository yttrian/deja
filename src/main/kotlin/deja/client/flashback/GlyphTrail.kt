package deja.client.flashback

import deja.client.animation.Animation.TICKS_PER_SECOND
import deja.client.animation.AnimationScreen
import deja.client.animation.FramedAnimationComponent
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.registry.Registry
import kotlin.math.ceil
import kotlin.random.Random

class GlyphTrail(screen: AnimationScreen) : FramedAnimationComponent(screen, Random.nextInt(EARLIEST_START_TIME, 0)) {
    private val trail = List(TRAIL_LENGTH) { Glyph(this, it) }
    private val posX = randomPosition(width)
    private val posY = randomPosition(height)
    private val shuffledSprites = sprites.shuffled().asWrap()

    private fun randomPosition(dimension: Int): Int {
        val bounds = dimension / 2

        return Random.nextInt(bounds + JITTER_MIN, bounds + JITTER_MAX) * if (Random.nextBoolean()) 1 else -1
    }

    /**
     * Render the glyph trail
     */
    override fun render(matrices: MatrixStack, time: Float) {
        trail.forEach {
            it.render(matrices, time)
        }

        if (time > TRAVEL_TIME) {
            rebase()
        }
    }

    private class Glyph(val glyphTrail: GlyphTrail, val position: Int) {
        // https://www.desmos.com/calculator/runxxwoydu
        private fun chooseSprite(time: Float) =
            glyphTrail.shuffledSprites[ceil((time / FREQUENCY + position + 1) / TRAIL_LENGTH) + position]

        fun render(matrices: MatrixStack, time: Float) {
            val z = glyphTrail
                .linearInterpolate(time, TRAVEL_TIME, Z_MIN + position * Z_SPACING, Z_MAX)
                .coerceAtLeast(0f)
            val x = glyphTrail.posX / z
            val y = glyphTrail.posY / z
            val sprite = chooseSprite(time)

            glyphTrail.drawSprite(matrices, sprite, x, y, 1 / z)
        }
    }

    private class WrapList<T>(val list: List<T>) : List<T> by list {
        override operator fun get(index: Int): T = list[index.rem(this.size)]
        operator fun get(index: Float): T = get(index.toInt())
        override fun toString() = list.toString()
    }

    private fun <T> List<T>.asWrap() = WrapList(this)

    companion object {
        private const val TRAIL_LENGTH = 10
        private const val Z_MIN = 12f
        private const val Z_MAX = 0f
        private const val Z_SPACING = 2f
        private const val JITTER_MIN = 50
        private const val JITTER_MAX = 125
        private const val FREQUENCY = 2f * TICKS_PER_SECOND / TRAIL_LENGTH
        private const val TRAVEL_TIME = 8 * TICKS_PER_SECOND
        private const val EARLIEST_START_TIME = -TRAVEL_TIME / 4

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
