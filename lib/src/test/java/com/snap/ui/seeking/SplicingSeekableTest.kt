package com.snap.ui.seeking

import com.google.common.collect.Lists
import com.snap.ui.seeking.Seekables.empty
import com.snap.ui.seeking.Seekables.splice
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SplicingSeekableTest {

    private lateinit var seekable1: Seekable<Int>
    private lateinit var seekable2: Seekable<Int>

    @Before
    fun setup() {
        seekable1 = ListSeekable(listOf(1, 2, 3, 4, 5))
        seekable2 = ListSeekable(listOf(10, 11, 12))
    }

    @Test
    @Throws(Exception::class)
    fun basic_splice() {
        var combined: Seekable<Int> = SplicingSeekable(seekable1, seekable2, 0)
        var list: List<Int> = combined.toList()
        Assert.assertEquals(Lists.newArrayList(10, 11, 12, 1, 2, 3, 4, 5), list)

        combined = SplicingSeekable(seekable1, seekable2, 3)
        list = combined.toList()
        Assert.assertEquals(listOf(1, 2, 3, 10, 11, 12, 4, 5), list)

        combined = SplicingSeekable(seekable1, seekable2, 8)
        list = combined.toList()
        Assert.assertEquals(listOf(1, 2, 3, 4, 5, 10, 11, 12), list)

        combined = SplicingSeekable(seekable1, empty(), 3)
        list = combined.toList()
        Assert.assertEquals(listOf(1, 2, 3, 4, 5), list)

        combined = splice(empty(), seekable1, 3)
        list = combined.toList()
        Assert.assertEquals(listOf(1, 2, 3, 4, 5), list)
    }
}
