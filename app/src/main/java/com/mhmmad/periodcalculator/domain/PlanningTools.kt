package com.mhmmad.periodcalculator.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class PlanningWindow(val first: LocalDate, val last: LocalDate, val central: LocalDate)
data class EventPlan(val windows: List<PlanningWindow>, val overlapDays: Int, val eventDays: Int)
data class ForecastTrial(val estimated: LocalDate, val actual: LocalDate, val lower: LocalDate, val upper: LocalDate) {
    val signedError: Long get() = ChronoUnit.DAYS.between(estimated, actual)
    val inWindow: Boolean get() = actual in lower..upper
}
object PlanningTools {
    fun event(today: LocalDate, offset: Int, duration: Int, history: List<PeriodRecord>, fallback: Int, bleeding: Int): EventPlan? {
        require(offset in 0..90 && duration in 1..14)
        val past = history.filter { !it.start.isAfter(today) }
        val prediction = CycleEngine.predict(past, fallback, today) ?: return null
        // Stale inputs cannot produce a dependable planning range.
        if (prediction.isExtrapolated || prediction.latest.isBefore(today)) return null
        val start = today.plusDays(offset.toLong())
        val end = start.plusDays(duration.toLong()-1)
        val length = prediction.averageDays.toLong().coerceIn(15,90)
        val bleed = bleeding.toLong().coerceIn(1,14)
        val base = maxOf(2L, ChronoUnit.DAYS.between(prediction.earliest, prediction.nextStart))
        val windows = (0..8).map { n ->
            val central = prediction.nextStart.plusDays(n * length)
            val spread = base + 2L * n
            PlanningWindow(central.minusDays(spread), central.plusDays(spread + bleed - 1), central)
        }.filter { !it.last.isBefore(today) && !it.first.isAfter(end) && !it.last.isBefore(start) }
        // Include observed completed or currently recorded bleeding, not only forecasts.
        val observed: List<Pair<LocalDate, LocalDate>> = past.map { p ->
            Pair(p.start, minOf(p.end ?: p.start, today))
        }.filter { (first, last) -> !last.isBefore(first) }
        val overlap = (0 until duration).count { index ->
            val day = start.plusDays(index.toLong())
            windows.any { day in it.first..it.last } || observed.any { (first, last) -> !day.isBefore(first) && !day.isAfter(last) }
        }
        return EventPlan(windows, overlap, duration)
    }

    /** Each trial uses only records strictly before the tested start; no fallback or future leakage. */
    fun audit(history: List<PeriodRecord>, today: LocalDate): List<ForecastTrial> {
        val ordered = history.filter { !it.start.isAfter(today) }.distinctBy { it.start }.sortedBy { it.start }
        return ordered.indices.drop(3).mapNotNull { i ->
            val known = ordered.subList(0, i)
            if (CycleEngine.intervals(known).size < 2) return@mapNotNull null
            val actual = ordered[i].start
            val p = CycleEngine.predict(known, 28, known.last().start) ?: return@mapNotNull null
            ForecastTrial(p.nextStart, actual, p.earliest, p.latest)
        }.takeLast(8)
    }
    fun meanAbsoluteError(trials: List<ForecastTrial>): Double? = trials.takeIf { it.isNotEmpty() }?.map { abs(it.signedError) }?.average()
}
