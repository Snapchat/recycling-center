package com.snap.ui.seeking

/**
 * A Seekable composed of two Seekables concatenated together.
 */
internal class AppendedSeekable<T>(
    private val head: Seekable<T>,
    private val tail: Seekable<T>
) : Seekable<T> {

    override fun size(): Int = head.size() + tail.size()

    override fun get(position: Int): T {
        val headSize = head.size()
        return if (position < headSize) {
            head[position]
        } else tail[position - headSize]
    }

    override fun iterator(): Iterator<T> = SeekableIterator(this)
}
