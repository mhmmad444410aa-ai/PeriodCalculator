package com.mhmmad.periodcalculator.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** Business logic must not depend on Android, the system clock's UTC offset, or ad SDKs. */
data class PeriodRecord(val id: Long, val start: LocalDate, val end: LocalDate?)

data class CyclePrediction(
    val nextStart: LocalDate,
    val earliest: LocalDate,
    val latest: LocalDate,
    val averageDays: Int,
    val intervals: List<Int>,
    val confidence: Confidence,
    val isExtrapolated: Boolean
)
enum class Confidence { LOW, MODERATE, HIGH }

data class FertilityWindow(val start: LocalDate, val end: LocalDate, val ovulation: LocalDate)

object CycleEngine {
    fun bleedingDays(record: PeriodRecord): Long? {
        val end = record.end ?: return null
        return if (end.isBefore(record.start)) null else ChronoUnit.DAYS.between(record.start, end) + 1
    }

    fun intervals(periods: List<PeriodRecord>): List<Int> = periods
        .map { it.start }
        .distinct()
        .sorted()
        .zipWithNext { first, second -> ChronoUnit.DAYS.between(first, second).toInt() }
        .filter { it in 15..90 } // plausibility screen; never diagnose abnormal cycles
        .takeLast(6)

    fun predict(periods: List<PeriodRecord>, fallbackDays: Int, today: LocalDate): CyclePrediction? {
        val latestPeriod = periods.maxByOrNull { it.start } ?: return null
        val cycles = intervals(periods)
        val sample = cycles.takeLast(6)
        val allStarts = periods.map { it.start }.distinct().sorted()
        val hasExcludedIntervals = allStarts.zipWithNext { a,b -> ChronoUnit.DAYS.between(a,b).toInt() }.any { it !in 15..90 }
        val estimated = if (sample.isEmpty()) fallbackDays.coerceIn(15, 90) else sample.average().roundToInt()
        val spread = if (sample.size < 2) 3 else ((sample.max() - sample.min()) / 2.0).roundToInt().coerceAtLeast(2)
        val confidence = when {
            sample.size < 3 || hasExcludedIntervals -> Confidence.LOW
            sample.max() - sample.min() > 10 -> Confidence.LOW
            sample.max() - sample.min() > 5 -> Confidence.MODERATE
            else -> Confidence.HIGH
        }
        var date = latestPeriod.start.plusDays(estimated.toLong())
        var extrapolations = 0
        while (date.isBefore(today.minusDays(5)) && extrapolations < 15) {
            date = date.plusDays(estimated.toLong())
            extrapolations++
        }
        val uncertainty = spread + (extrapolations * 2)
        return CyclePrediction(date, date.minusDays(uncertainty.toLong()),
            date.plusDays(uncertainty.toLong()), estimated, sample, confidence,
            extrapolations > 0)
    }

    /** Calendar-only estimate. Not for contraception, conception or confirmation. */
    fun fertility(prediction: CyclePrediction): FertilityWindow? {
        if (prediction.confidence == Confidence.LOW || prediction.isExtrapolated) return null
        val ovulation = prediction.nextStart.minusDays(14)
        return FertilityWindow(ovulation.minusDays(5), ovulation.plusDays(1), ovulation)
    }

    fun currentCycleDay(periods: List<PeriodRecord>, today: LocalDate): Long? {
        val last = periods.filter { !it.start.isAfter(today) }.maxByOrNull { it.start } ?: return null
        val day = ChronoUnit.DAYS.between(last.start, today) + 1
        return if (day in 1..90) day else null
    }
    fun variation(intervals: List<Int>): Int = if (intervals.isEmpty()) 0 else intervals.max() - intervals.min()
    fun numberOfDaysUntil(date: LocalDate, today: LocalDate): Long = ChronoUnit.DAYS.between(today, date)
}
