package com.snap.ui.seeking

/**
 * A Seekable made by concatenating a list of Seekables together.
 */
class ConcatSeekable<T>(private val list: List<Seekable<T>>) : Seekable<T> {

    override fun get(position: Int): T {
        var positionInSection = position
        for (i in 0 until list.size) {
            val section = list[i]
            val sectionSize = section.size()
            if (positionInSection < sectionSize) {
                return section[positionInSection]
            }
            positionInSection -= sectionSize
        }
        throw IndexOutOfBoundsException()
    }

    override fun size() = list.sumBy(Seekable<T>::size)

    override fun iterator(): Iterator<T> {
        return SeekableIterator(this)
    }
}
