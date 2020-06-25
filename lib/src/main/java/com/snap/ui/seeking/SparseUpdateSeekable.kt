package com.snap.ui.seeking

import android.util.SparseArray

/**
 * A Seekable built on top of another Seekable, with a sparse layer of overlayed updates.
 */
class SparseUpdateSeekable<T>(private val source: Seekable<T>) : Seekable<T> {

    private val updates = SparseArray<T>()

    override fun size() = source.size()
    override fun iterator() = SeekableIterator(this)
    override fun get(position: Int) = updates.get(position, source[position])

    /**
     * Updates the item at a specific position.
     *
     * @throws IndexOutOfBoundsException throws [IndexOutOfBoundsException] if the updating position exceeds
     * seekable size.
     */
    fun update(pos: Int, item: T) {
        if (pos >= size()) {
            throw IndexOutOfBoundsException()
        }
        updates.put(pos, item)
    }
}
