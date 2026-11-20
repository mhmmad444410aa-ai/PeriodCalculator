package com.mhmmad.periodcalculator.domain

data class VisitDay(val day: Long, val levels: List<Int>)
data class VisitPeriod(val start: Long, val end: Long?)
data class VisitSummary(val days: Int, val logged: Int, val periodStarts: Int, val completedLengths: List<Long>, val markedDays: List<Int>)
object VisitSummaries {
    fun build(today: Long, days: Int, periods: List<VisitPeriod>, entries: List<VisitDay>): VisitSummary {
        require(days in 1..365)
        val first = today - days + 1
        val actual = entries.filter { it.day in first..today && it.levels.size == 4 && it.levels.all { n -> n in 0..3 } }.distinctBy { it.day }
        val starts = periods.filter { it.start in first..today }.distinctBy { it.start }
        val lengths = starts.mapNotNull { p -> p.end?.takeIf { it in p.start..today }?.let { it - p.start + 1 } }
        return VisitSummary(days, actual.size, starts.size, lengths, (0..3).map { i -> actual.count { it.levels[i] >= 2 } })
    }
}
