package com.snap.ui.seeking

import com.google.common.collect.Lists
import org.junit.Assert
import org.junit.Test

class SparseUpdateSeekableTest {

    @Test
    fun testSparseUpdate_withValidUpdates_listUpdatedSuccessfully() {
        val sparseUpdatedList = SparseUpdateSeekable(ListSeekable<Int>(listOf(1, 2, 3)))

        sparseUpdatedList.update(2, 4)
        Assert.assertEquals(Lists.newArrayList(sparseUpdatedList), listOf(1, 2, 4))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testSparseUpdate_withInvalidUpdates_throwException() {
        val sparseUpdatedList = SparseUpdateSeekable(ListSeekable<Int>(listOf(1, 2, 3)))
        sparseUpdatedList.update(3, 4)
    }
}
