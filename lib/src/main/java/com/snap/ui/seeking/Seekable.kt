package com.snap.ui.seeking

/**
 * Interface for random-access lookups of a fixed number of items.
 */
interface Seekable<out T> : Iterable<T> {
    /**
     * Number of items in this data set.
     */
    fun size(): Int

    /**
     * Returns the item at the given position.
     * @param position
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to [.size].
     */
    operator fun get(position: Int): T
}
