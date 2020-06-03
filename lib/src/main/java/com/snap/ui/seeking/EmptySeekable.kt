package com.snap.ui.seeking

import java.util.Collections

/**
 * A seekable with zero items.
 */
internal object EmptySeekable : Seekable<Nothing> {

    override fun get(position: Int): Nothing {
        throw IndexOutOfBoundsException()
    }

    override fun size(): Int = 0

    override fun iterator(): Iterator<Nothing> = Collections.emptyIterator()
}
