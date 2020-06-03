package com.snap.ui.seeking

import com.google.common.collect.Lists
import org.junit.Assert
import org.junit.Test

class ConcatSeekableTest {

    @Test
    fun test_concat() {
        val a = ListSeekable(listOf(1, 2, 3))
        val b = ListSeekable<Int>(Lists.newArrayList(
                Seekables.concat(Seekables.of(4), Seekables.of(5))
        ))
        val c = ListSeekable(listOf(6))
        val d = ConcatSeekable(listOf(Seekables.of(7), Seekables.of(8)))
        val e = Seekables.empty<Int>()

        val concat = Seekables.concat(listOf(a, b, c, d, e))
        val list = Lists.newArrayList<Int>(concat)
        Assert.assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), list)

        try {
            concat[11]
            Assert.fail()
        } catch (e: IndexOutOfBoundsException) {}
    }
}
