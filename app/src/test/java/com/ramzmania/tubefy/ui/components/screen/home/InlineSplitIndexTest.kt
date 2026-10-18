package com.ramzmania.tubefy.ui.components.screen.home

import org.junit.Assert.assertEquals
import org.junit.Test

class InlineSplitIndexTest {

    @Test
    fun `fourth item with the top grid header comes after 2 sections`() {
        assertEquals(2, inlineSplitIndex(4, hasHeader = true, sectionCount = 10))
    }

    @Test
    fun `fourth item without a header comes after 3 sections`() {
        assertEquals(3, inlineSplitIndex(4, hasHeader = false, sectionCount = 10))
    }

    @Test
    fun `fewer sections than the position puts it after the last section`() {
        assertEquals(1, inlineSplitIndex(4, hasHeader = false, sectionCount = 1))
        assertEquals(2, inlineSplitIndex(4, hasHeader = true, sectionCount = 2))
        assertEquals(0, inlineSplitIndex(4, hasHeader = true, sectionCount = 0))
    }

    @Test
    fun `never out of range for odd positions`() {
        for (count in 0..6) for (position in -2..10) for (header in listOf(true, false)) {
            val split = inlineSplitIndex(position, header, count)
            val sections = List(count) { it }
            // take/drop with this index must cover every section exactly once
            assertEquals(sections, sections.take(split) + sections.drop(split))
            assert(split in 0..count)
        }
    }
}

class InlineSplitIndicesTest {

    @Test
    fun `jump back in 4th and recommended 6th with the top grid`() {
        // grid(1) s1(2) s2(3) jump(4) s3(5) recommended(6)
        assertEquals(listOf(2, 3), inlineSplitIndices(listOf(4, 6), hasHeader = true, sectionCount = 10))
    }

    @Test
    fun `without the top grid`() {
        // s1 s2 s3 jump(4) s4 recommended(6)
        assertEquals(listOf(3, 4), inlineSplitIndices(listOf(4, 6), hasHeader = false, sectionCount = 10))
    }

    @Test
    fun `only recommended present`() {
        // grid(1) s1..s4 (2..5) recommended(6)
        assertEquals(listOf(4), inlineSplitIndices(listOf(6), hasHeader = true, sectionCount = 10))
    }

    @Test
    fun `few sections puts blocks after the last one, in order`() {
        assertEquals(listOf(1, 1), inlineSplitIndices(listOf(4, 6), hasHeader = false, sectionCount = 1))
        assertEquals(listOf(0, 0), inlineSplitIndices(listOf(4, 6), hasHeader = true, sectionCount = 0))
    }

    @Test
    fun `always in range and non decreasing`() {
        for (count in 0..8) for (a in -1..9) for (b in a..12) for (header in listOf(true, false)) {
            val splits = inlineSplitIndices(listOf(a, b), header, count)
            assert(splits.all { it in 0..count }) { "$splits out of 0..$count" }
            assert(splits[0] <= splits[1])
            val sections = List(count) { it }
            var start = 0
            val rebuilt = mutableListOf<Int>()
            splits.forEach { split -> rebuilt += sections.drop(start).take(split - start); start = split }
            rebuilt += sections.drop(start)
            assertEquals(sections, rebuilt)
        }
    }
}
