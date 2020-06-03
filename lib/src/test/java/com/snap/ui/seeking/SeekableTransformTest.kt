package com.snap.ui.seeking

import com.google.common.collect.Lists
import org.junit.Assert
import org.junit.Test

class SeekableTransformTest {

    @Test
    fun test_transform() {
        val a = ListSeekable<Int>(listOf(0, 1, 2, 3))
        val b = ListSeekable<Int>(listOf(4, 5))

        val input = Seekables.concat(a, b)
        val res = Seekables.map(input) { x, i -> (x + i) }
        val output = Lists.newArrayList<Int>(res)

        val expected = listOf(0, 2, 4, 6, 8, 10)
        Assert.assertEquals(expected, output)
    }
}
