package com.mhmmad.periodcalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class CycleMathTest {
    @Test fun twentyEightDayCycle() {
        val dates = listOf(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-29"))
        assertEquals(28, CycleMath.averageCycle(dates))
        assertEquals(LocalDate.parse("2026-10-27"), CycleMath.nextExpected(dates, LocalDate.parse("2026-10-01")))
    }

    @Test fun leapYearAndMonths() {
        assertEquals(3, CycleMath.bleedingDays(LocalDate.parse("2024-02-28"), LocalDate.parse("2024-03-01")))
    }

    @Test fun notEnoughHistory() {
        assertNull(CycleMath.nextExpected(emptyList(), LocalDate.parse("2026-11-13")))
    }

    @Test fun skipsPastPredictions() {
        val next = CycleMath.nextExpected(listOf(LocalDate.parse("2026-01-01")), LocalDate.parse("2026-03-15"))
        assertEquals(LocalDate.parse("2026-03-26"), next)
    }
}
