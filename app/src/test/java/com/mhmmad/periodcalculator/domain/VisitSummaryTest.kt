package com.mhmmad.periodcalculator.domain
import org.junit.Assert.*
import org.junit.Test
class VisitSummaryTest {
    @Test fun emptyDoesNotInventRecords() {
        val r = VisitSummaries.build(100, 30, emptyList(), emptyList())
        assertEquals(0, r.logged); assertEquals(0, r.periodStarts); assertTrue(r.completedLengths.isEmpty())
    }
    @Test fun boundariesDuplicatesAndFuture() {
        val r = VisitSummaries.build(100, 30, listOf(VisitPeriod(71,75), VisitPeriod(100,null), VisitPeriod(101,102)),
            listOf(VisitDay(70,listOf(3,3,3,3)), VisitDay(71,listOf(2,0,1,3)), VisitDay(71,listOf(3,3,3,3)), VisitDay(100,listOf(0,2,3,0)), VisitDay(101,listOf(3,3,3,3))))
        assertEquals(2,r.logged); assertEquals(2,r.periodStarts); assertEquals(listOf(5L),r.completedLengths); assertEquals(listOf(1,1,1,1),r.markedDays)
    }
    @Test fun invalidAndUnfinishedExcluded() {
        val r = VisitSummaries.build(100,30,listOf(VisitPeriod(90, 101),VisitPeriod(95,94)),listOf(VisitDay(90,listOf(5,0,0,0))))
        assertEquals(0,r.logged); assertTrue(r.completedLengths.isEmpty())
    }
}
