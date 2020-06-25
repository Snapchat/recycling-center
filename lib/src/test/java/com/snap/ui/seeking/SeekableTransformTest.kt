package com.snap.ui.seeking

import org.junit.Assert
import org.junit.Test

class SeekableTransformTest {

    @Test
    fun test_transform() {
        val a = ListSeekable(listOf(0, 1, 2, 3))
        val b = ListSeekable(listOf(4, 5))

        val input = Seekables.concat(a, b)
        val res = Seekables.map(input) { x, i -> (x + i) }
        val output = res.toList()

        val expected = listOf(0, 2, 4, 6, 8, 10)
        Assert.assertEquals(expected, output)
    }
}
