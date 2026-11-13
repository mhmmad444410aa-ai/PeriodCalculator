package com.mhmmad.periodcalculator

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** Date-only calculations; never derive calendar days from millisecond durations. */
object CycleMath {
    fun cycleLengths(starts: List<LocalDate>): List<Int> =
        starts.distinct().sorted().zipWithNext { a, b ->
            ChronoUnit.DAYS.between(a, b).toInt()
        }.filter { it in 15..90 }

    fun averageCycle(starts: List<LocalDate>, defaultLength: Int = 28): Int {
        val recent = cycleLengths(starts).takeLast(6)
        return if (recent.isEmpty()) defaultLength.coerceIn(15, 90)
        else recent.average().roundToInt().coerceIn(15, 90)
    }

    fun nextExpected(starts: List<LocalDate>, today: LocalDate, defaultLength: Int = 28): LocalDate? {
        val last = starts.maxOrNull() ?: return null
        val interval = averageCycle(starts, defaultLength).toLong()
        var next = last.plusDays(interval)
        while (next.isBefore(today)) next = next.plusDays(interval)
        return next
    }

    fun bleedingDays(start: LocalDate, end: LocalDate): Int =
        (ChronoUnit.DAYS.between(start, end) + 1).toInt().coerceAtLeast(1)

    fun daysUntil(target: LocalDate, today: LocalDate): Int =
        ChronoUnit.DAYS.between(today, target).toInt()
}
