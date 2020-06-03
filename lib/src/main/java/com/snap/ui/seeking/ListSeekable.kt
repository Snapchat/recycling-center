package com.snap.ui.seeking

/**
 * Wraps a [java.util.List] as a [Seekable].
 */

data class ListSeekable<T>(private val list: List<T>) : Seekable<T> {

    override fun get(position: Int): T = list[position]

    override fun size(): Int = list.size

    override fun iterator(): Iterator<T> = SeekableIterator(this)

    override fun toString(): String = list.toString()
}
