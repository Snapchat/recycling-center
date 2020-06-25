package com.snap.ui.seeking

import android.database.Cursor
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Wraps a [android.database.Cursor] as a [Seekable]. This class is disposable
 * and will close the underlying Cursor upon disposal.
 */

class CursorSeekable<T>(
    private val cursor: Cursor,
    internal var mapper: Function<Cursor, T>
) : Seekable<T>, Disposable {

    private val disposed = AtomicBoolean(false)

    /**
     * Returns the count of the underlying cursor. Note that this forces a read
     * of the data set. You may want to ensure the first call to this method or [Cursor.getCount]
     * occurs on a background thread.
     */
    override fun size(): Int = cursor.count

    override fun get(position: Int): T {
        if (position < 0 || position >= cursor.count) {
            throw IndexOutOfBoundsException()
        }

        cursor.moveToPosition(position)
        try {
            return mapper.apply(cursor)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            cursor.close()
        }
    }

    override fun isDisposed(): Boolean {
        return disposed.get()
    }

    override fun iterator(): Iterator<T> {
        return SeekableIterator(this)
    }
}
