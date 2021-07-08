package deja.client.animation

/**
 * Defines a spread of items
 */
data class Spread(
    /**
     * The unique index given to the current holder of this spread
     */
    val current: Int,
    /**
     * The total number of items in the spread
     */
    val total: Int
) {
    /**
     * current / total
     */
    val fraction: Float = current.toFloat() / total

    /**
     * Do we belong to the upper half of the spread?
     */
    val isUpper = fraction >= HALF

    /**
     * Do we belong to the lower half of the spread?
     */
    val isLower = !isUpper

    /**
     * Split the spread to get a spread of the half it is in
     */
    fun split(): Spread = if (isUpper) {
        Spread(current / 2, total / 2)
    } else {
        Spread(current, total / 2)
    }

    companion object {
        private const val HALF = 0.5f
    }
}
