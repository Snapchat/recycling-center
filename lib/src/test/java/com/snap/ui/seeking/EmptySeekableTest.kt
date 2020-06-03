package com.snap.ui.seeking

import com.google.common.collect.Lists
import org.junit.Assert
import org.junit.Test

class EmptySeekableTest {

    @Test
    fun test_empty() {
        val empty = Seekables.empty<Int>()
        Assert.assertEquals(0, empty.size())
        try {
            empty.get(1)
            Assert.fail()
        } catch (e: IndexOutOfBoundsException) {}

        val list = Lists.newArrayList<Int>(empty)
        Assert.assertEquals(0, list.size)
    }
}