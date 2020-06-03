package com.snap.ui.seeking

import com.google.common.collect.Lists
import org.junit.Assert
import org.junit.Test

class ListSeekableTest {

    @Test
    fun test_concat() {
        val input = listOf(1, 2, 3)
        val a = ListSeekable<Int>(input)
        val list = Lists.newArrayList<Int>(a)

        Assert.assertEquals(input, list)
        Assert.assertEquals(3, a.size())
        try {
            a.get(4)
            Assert.fail()
        } catch (e: IndexOutOfBoundsException) {}
    }
}