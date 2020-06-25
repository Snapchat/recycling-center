package com.snap.ui.seeking

import kotlin.math.min

/**
 * A [Seekable] that splices another Seekable at the given position.
 * If `spliceAt` is beyond the length of `content`, then `splice`
 * is appended to the end of `content`.
 */
class SplicingSeekable<T>(
    private val content: Seekable<T>,
    private val splice: Seekable<T>,
    private val splicePosition: Int
) : Seekable<T> {

    override fun get(position: Int): T {
        if (position < splicePosition) {
            return if (position < content.size()) {
                content[position]
            } else {
                splice[position - content.size()]
            }
        } else {
            val contentSeen = min(splicePosition, content.size())
            return if (splicePosition < content.size()) {
                val offset = position - contentSeen
                if (offset < splice.size()) {
                    splice[offset]
                } else {
                    content[position - splice.size()]
                }
            } else {
                splice[position - content.size()]
            }
        }
    }

    override fun size(): Int {
        return content.size() + splice.size()
    }

    override fun iterator(): Iterator<T> {
        return SeekableIterator(this)
    }
}
