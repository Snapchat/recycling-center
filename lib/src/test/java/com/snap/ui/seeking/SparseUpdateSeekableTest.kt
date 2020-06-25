package com.snap.ui.seeking

import org.junit.Assert
import org.junit.Test

class SparseUpdateSeekableTest {

    @Test
    fun testSparseUpdate_withValidUpdates_listUpdatedSuccessfully() {
        val sparseUpdatedList = SparseUpdateSeekable(ListSeekable(listOf(1, 2, 3)))

        sparseUpdatedList.update(2, 4)
        Assert.assertEquals(sparseUpdatedList.toList(), listOf(1, 2, 4))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testSparseUpdate_withInvalidUpdates_throwException() {
        val sparseUpdatedList = SparseUpdateSeekable(ListSeekable(listOf(1, 2, 3)))
        sparseUpdatedList.update(3, 4)
    }
}
