package deja.client.flashback

import deja.client.animation.AnimationScreen
import deja.client.animation.FramedAnimationComponent
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.registry.Registry
import kotlin.math.floor
import kotlin.random.Random

class GlyphTrail(screen: AnimationScreen) : FramedAnimationComponent(screen, Random.nextInt(-TRAVEL_TIME, 0)) {
    private val trail = List(TRAIL_LENGTH) { Glyph(this, it) }
    private val posX = randomPosition(width)
    private val posY = randomPosition(height)

    private fun randomPosition(dimension: Int): Int {
        val bounds = dimension / 2

        return Random.nextInt(bounds, bounds + JITTER) * if (Random.nextBoolean()) 1 else -1
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
        private val shuffledSprites = sprites.shuffled()
        private fun chooseSprite(time: Float) = shuffledSprites[floor(time / FREQUENCY).toInt().rem(sprites.size)]

        fun render(matrices: MatrixStack, time: Float) {
            val sprite = chooseSprite(time)
            val z = glyphTrail
                .linearInterpolate(time, TRAVEL_TIME, Z_MIN + position * Z_SPACING, Z_MAX)
                .coerceAtLeast(0f)
            val x = glyphTrail.posX / z
            val y = glyphTrail.posY / z
            val width = sprite.width / z / DOWNSCALE
            val height = sprite.height / z / DOWNSCALE

            glyphTrail.drawImage(matrices, sprite.atlas.id, x, y, width, height, sprite)
        }
    }

    companion object {
        private const val TRAIL_LENGTH = 7
        private const val Z_MIN = 10f
        private const val Z_MAX = 0f
        private const val Z_SPACING = 2.5f
        private const val JITTER = 100
        private const val FREQUENCY = 1 * AnimationScreen.TICKS_PER_SECOND
        private const val TRAVEL_TIME = 10 * AnimationScreen.TICKS_PER_SECOND
        private const val DOWNSCALE = 1.5f

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
