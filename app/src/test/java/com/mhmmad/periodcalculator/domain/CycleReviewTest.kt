package com.mhmmad.periodcalculator.domain
import org.junit.Assert.*
import org.junit.Test
class CycleReviewTest {
    private fun sample(day: Long, value: Int) = ReviewSample(day, List(4) { value })
    @Test fun windowsAreNotOverlapping() {
        val cycles = CycleReview.build(listOf(10,40,40,70,200), listOf(sample(39,2),sample(40,0),sample(70,3),sample(91,1)),90)
        assertEquals(listOf(70L,40L,10L),cycles.map { it.start })
        assertEquals(listOf(21L,30L,30L),cycles.map { it.length })
        assertFalse(cycles.first().complete)
        assertEquals(0,cycles[1].level(40,0))
        assertNull(cycles[1].level(41,0))
        assertEquals(2.0,cycles[2].average(0)!!,0.0)
    }
    @Test fun emptyAndMissingAreNotZero() {
        assertTrue(CycleReview.build(emptyList(),emptyList(),100).isEmpty())
        val c = CycleReview.build(listOf(100),listOf(sample(100,9)),100).single()
        assertEquals(1L,c.length)
        assertNull(c.average(0))
        assertNull(c.level(100,0))
    }
    @Test fun duplicateLogsAndFutureDatesAreExcluded() {
        val c = CycleReview.build(listOf(5),listOf(sample(5,0),sample(5,3),sample(9,3)),8).single()
        assertEquals(1,c.samples.size)
        assertEquals(0.0,c.average(0)!!,0.0)
    }
}
