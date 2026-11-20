package com.mhmmad.periodcalculator.domain
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class PlanningToolsTest {
    private val today = LocalDate.of(2026, 11, 20)
    private fun record(days: Long) = PeriodRecord(days, today.plusDays(days), null)
    @Test fun futureOnlyDoesNotPredict() {
        assertNull(PlanningTools.event(today,7,3,listOf(record(2)),28,5))
    }
    @Test fun overlapUsesUnionNotSum() {
        val r = PlanningTools.event(today,0,14,listOf(record(-26)),28,5)!!
        assertEquals(10,r.overlapDays)
        assertEquals(14,r.eventDays)
    }
    @Test fun noOverlapIsZero() {
        assertEquals(0,PlanningTools.event(today,10,2,listOf(record(-5)),28,5)!!.overlapDays)
    }
    @Test fun auditNeverUsesLaterStarts() {
        val rows = listOf(record(-84),record(-56),record(-28),record(0),record(29))
        val r = PlanningTools.audit(rows,today)
        assertEquals(1,r.size)
        assertEquals(0L,r.single().signedError)
        assertTrue(r.single().inWindow)
        assertEquals(0.0,PlanningTools.meanAbsoluteError(r)!!,0.01)
    }
    @Test fun tooFewRecordsHaveNoAudit() {
        assertTrue(PlanningTools.audit(listOf(record(-56),record(-28),record(0)),today).isEmpty())
    }
}
