package com.snap.ui.seeking

/**
 * A Seekable that reverses another Seekable.
 */
internal class ReversingSeekable<T>(private val source: Seekable<T>) : Seekable<T> {

    override fun get(position: Int): T = source[size() - position - 1]

    override fun size(): Int = source.size()

    override fun iterator(): Iterator<T> = SeekableIterator(this)
}
