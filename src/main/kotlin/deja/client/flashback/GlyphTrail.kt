package deja.client.flashback

import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.registry.Registry

class GlyphTrail(var startTime: Int) {
    private val trail = List(TRAIL_LENGTH) { createShuffledGlyph(this, it) }

    /**
     * Render the glyph trail
     */
    fun render(matrices: MatrixStack, time: Int) {
        trail.forEach {
            it.render(matrices)
        }
    }

    companion object {
        private const val TRAIL_LENGTH = 5

        private val sprites = MinecraftClient.getInstance().let {
            val particleManagerAccessor = it.particleManager as ParticleManagerAccessor
            val enchantParticleType = Registry.PARTICLE_TYPE.getId(ParticleTypes.ENCHANT)
            val spriteAwareFactories = particleManagerAccessor.spriteAwareFactories
            val enchantParticleSpriteProviderAccessor =
                spriteAwareFactories[enchantParticleType] as ParticleManagerAccessor.SimpleSpriteProviderAccessor
            enchantParticleSpriteProviderAccessor.sprites.toList()
        }

        private class ShuffledParticle(glyphTrail: GlyphTrail, val position: Int, val sprites: List<Sprite>) {
            fun render(matrices: MatrixStack) {
                matrices.push()
            }
        }

        private fun createShuffledGlyph(glyphTrail: GlyphTrail, position: Int) =
            ShuffledParticle(glyphTrail, position, sprites.shuffled())
    }
}
