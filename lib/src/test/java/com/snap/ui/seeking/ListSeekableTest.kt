package com.snap.ui.seeking

import org.junit.Assert
import org.junit.Test

class ListSeekableTest {

    @Test
    fun test_concat() {
        val input = listOf(1, 2, 3)
        val a = ListSeekable(input)
        val list = a.toList()

        Assert.assertEquals(input, list)
        Assert.assertEquals(3, a.size())
        try {
            a[4]
            Assert.fail()
        } catch (e: IndexOutOfBoundsException) {}
    }
}
