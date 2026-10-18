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
