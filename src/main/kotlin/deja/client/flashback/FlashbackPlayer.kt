package deja.client.flashback

import deja.client.animation.Animation.TICKS_PER_SECOND
import deja.client.animation.AnimationScreen
import deja.client.animation.Spread
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import java.awt.Color

/**
 * Screen for playing flashbacks
 */
class FlashbackPlayer(rawMemories: MutableList<NativeImage>) : AnimationScreen(LiteralText("deja.flashback")) {
    private val mask = Mask(this, 0, MASK_TIME)
    private val maskFade = FadeBlack(this, 0, MASK_FADE_TIME)
    private val memoryTextures = rawMemories.asReversed().map { it.toTexture("memory") }.also { rawMemories.clear() }
    private val memories = Memories(this, MEMORY_TIME_START, memoryTextures)
    private val memoriesFade = FadeBlack(this, MEMORY_TIME_START, MEMORY_FADE_TIME)
    private val glyphTrails = List(GLYPH_TRAILS) { GlyphTrail(this, Spread(it, GLYPH_TRAILS)) }

    /**
     * Render loop
     */
    override fun render(matrices: MatrixStack) {
        drawMemories(matrices)
        drawMask(matrices)
    }

    private fun drawMask(matrices: MatrixStack) {
        mask.render(matrices)
        if (time < MASK_FADE_TIME) {
            maskFade.render(matrices)
        }
    }

    private fun drawMemories(matrices: MatrixStack) {
        if (time > GLYPH_TIME_START) {
            glyphTrails.forEach { it.render(matrices) }
        }
        memories.render(matrices)
        if (time < (MEMORY_TIME_START + MEMORY_FADE_TIME)) {
            memoriesFade.render(matrices)
        }
    }

    /**
     * Render a solid black background
     */
    override fun renderBackground(matrices: MatrixStack) {
        DrawableHelper.fill(matrices, 0, 0, width, height, Color.BLACK.rgb)
    }

    /**
     * Cleanup on close
     */
    override fun onClose() {
        memoryTextures.forEach { it.destroy() }
        super.onClose()
    }

    companion object {
        private const val GLYPH_TRAILS: Int = 15

        /**
         * The duration of the mask approach
         */
        const val MASK_TIME: Int = 6 * TICKS_PER_SECOND
        private const val MEMORY_TIME_START: Int = (MASK_TIME * 0.5f).toInt()
        private const val GLYPH_TIME_START: Int = (MASK_TIME * 0.6f).toInt()
        private const val MASK_FADE_TIME: Int = 4 * TICKS_PER_SECOND
        private const val MEMORY_FADE_TIME: Int = 3 * TICKS_PER_SECOND
    }
}
