package com.snap.ui.seeking

class SeekableIterator<T>(private val seekable: Seekable<T>) : Iterator<T> {
    private var position = 0

    override fun hasNext(): Boolean {
        return position < seekable.size()
    }

    override fun next(): T {
        return seekable[position++]
    }
}
