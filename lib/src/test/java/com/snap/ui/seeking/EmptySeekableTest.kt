package com.snap.ui.seeking

import org.junit.Assert
import org.junit.Test

class EmptySeekableTest {

    @Test
    fun test_empty() {
        val empty = Seekables.empty<Int>()
        Assert.assertEquals(0, empty.size())
        try {
            empty[1]
            Assert.fail()
        } catch (e: IndexOutOfBoundsException) {
        }

        val list = empty.toList()
        Assert.assertEquals(0, list.size)
    }
}
