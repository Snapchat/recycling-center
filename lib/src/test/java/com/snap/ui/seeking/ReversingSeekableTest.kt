package com.snap.ui.seeking

import com.snap.ui.seeking.Seekables.reverse
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ReversingSeekableTest {

    private lateinit var seekable: Seekable<Int>

    @Before
    fun setup() {
        seekable = ListSeekable(listOf(1, 2, 3))
    }

    @Test
    @Throws(Exception::class)
    fun basic_test() {
        val reversed = reverse(seekable)
        val list = reversed.toList()
        Assert.assertEquals(listOf(3, 2, 1), list)

        val forward = reverse(list)
        val list2: List<Int> = forward.toList()
        Assert.assertEquals(listOf(1, 2, 3), list2)
    }
}
