package com.snap.ui.seeking

/**
 * A Seekable that maps each item from a given Seekable.
 */
class SeekableTransform<S, T> internal constructor(
    private val source: Seekable<S>,
    private val mapping: (s: S, position: Int) -> T
) : Seekable<T> {

    override fun get(position: Int): T {
        return map(source[position], position)
    }

    override fun size(): Int {
        return source.size()
    }

    private fun map(item: S, position: Int): T {
        return mapping.invoke(item, position)
    }

    override fun iterator(): Iterator<T> {
        return SeekableIterator(this)
    }
}
