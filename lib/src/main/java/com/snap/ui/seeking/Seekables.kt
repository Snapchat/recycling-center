package com.snap.ui.seeking

/**
 * Creators for common types of Seekables.
 */
object Seekables {

    /**
     * A seekable with zero items.
     */
    @JvmStatic
    fun <T> empty(): Seekable<T> {
        return EmptySeekable
    }

    /**
     * Creates an immutable Seekable from the given list.
     */
    @JvmStatic
    fun <T> copyOf(list: List<T>): Seekable<T> {
        return ListSeekable(list.toList())
    }

    /**
     * Creates a Seekable reversing a list.
     */
    @JvmStatic
    fun <T> reverse(list: List<T>): Seekable<T> {
        return ReversingSeekable(copyOf(list))
    }

    /**
     * Creates a Seekable reversing another Seekable.
     */
    @JvmStatic
    fun <T> reverse(source: Seekable<T>): Seekable<T> {
        return ReversingSeekable(source)
    }

    /**
     * Returns a Seekable of a single item.
     */
    @JvmStatic
    fun <T> of(item: T): Seekable<T> {
        return ListSeekable(listOf(item))
    }

    @JvmStatic
    fun <S, T> map(seekable: Seekable<S>, mapping: (s: S, position: Int) -> T): Seekable<T> {
        return SeekableTransform(seekable, mapping)
    }

    @JvmStatic
    fun <T> concat(head: Seekable<T>, tail: Seekable<T>): Seekable<T> {
        return AppendedSeekable(head, tail)
    }

    @JvmStatic
    fun <T> concat(seekables: List<Seekable<T>>): Seekable<T> {
        return ConcatSeekable(seekables)
    }

    /**
     * Returns a [Seekable] that splices another Seekable at the given position.
     * If `spliceAt` is beyond the length of `content`, then `splice`
     * is appended to the end of `content`.
     */
    @JvmStatic
    fun <T> splice(content: Seekable<T>, splice: Seekable<T>, splicePosition: Int): Seekable<T> {
        return SplicingSeekable(content, splice, splicePosition)
    }
}
