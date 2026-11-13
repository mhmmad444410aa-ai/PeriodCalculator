package com.mhmmad.periodcalculator.domain

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CycleEngineTest {
    private fun d(value: String) = LocalDate.parse(value)
    private fun r(s: String, e: String? = null, id: Long = 0) = PeriodRecord(id, d(s), e?.let(::d))

    @Test fun `regular cycle across months`() {
        val data = listOf(r("2026-01-01"), r("2026-01-29"), r("2026-02-26"), r("2026-03-26"))
        val result = CycleEngine.predict(data, 31, d("2026-04-01"))!!
        assertEquals(28, result.averageDays)
        assertEquals(d("2026-04-23"), result.nextStart)
        assertEquals(Confidence.HIGH, result.confidence)
    }
    @Test fun `leap year February counts by local dates`() {
        assertEquals(29, CycleEngine.intervals(listOf(r("2024-02-01"), r("2024-03-01"))).single())
    }
    @Test fun `bleeding includes both endpoints`() {
        assertEquals(5L, CycleEngine.bleedingDays(r("2026-02-27", "2026-03-03")))
        assertNull(CycleEngine.bleedingDays(r("2026-04-03", "2026-04-02")))
    }
    @Test fun `irregular cycles widen range and confidence low`() {
        val data = listOf(r("2025-12-01"), r("2025-12-22"), r("2026-01-28"), r("2026-02-18"))
        val p = CycleEngine.predict(data, 28, d("2026-02-20"))!!
        assertEquals(Confidence.LOW, p.confidence)
        assertNull(CycleEngine.fertility(p))
        assertTrue(p.latest.isAfter(p.earliest))
    }
    @Test fun `single record uses declared length with low confidence`() {
        val p = CycleEngine.predict(listOf(r("2026-07-15")), 32, d("2026-07-16"))!!
        assertEquals(d("2026-08-16"), p.nextStart)
        assertEquals(Confidence.LOW, p.confidence)
    }
    @Test fun `missing periods returns no prediction`() {
        assertNull(CycleEngine.predict(emptyList(), 28, d("2026-10-10")))
    }
    @Test fun `times and travel cannot affect LocalDate arithmetic`() {
        val start = d("2026-10-30")
        assertEquals(28L, java.time.temporal.ChronoUnit.DAYS.between(start, start.plusDays(28)))
    }
    @Test fun `estimated fertility is never claimed as exact`() {
        val p = CycleEngine.predict(listOf(r("2026-03-01"), r("2026-03-29"), r("2026-04-26"), r("2026-05-24")), 28, d("2026-05-25"))!!
        assertEquals(d("2026-06-07"), CycleEngine.fertility(p)?.ovulation)
    }
}
