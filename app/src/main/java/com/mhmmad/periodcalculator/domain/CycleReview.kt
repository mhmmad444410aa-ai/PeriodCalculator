package com.mhmmad.periodcalculator.domain

/** Recorded days are distinct from missing data. No diagnosis or predictive claims. */
data class ReviewSample(val day: Long, val levels: List<Int>)
data class ReviewCycle(val start: Long, val endExclusive: Long, val complete: Boolean, val samples: List<ReviewSample>) {
    val length: Long get() = endExclusive - start
    fun level(day: Long, metric: Int): Int? = samples.firstOrNull { it.day == day }?.levels?.getOrNull(metric)
    fun average(metric: Int): Double? = samples.mapNotNull { it.levels.getOrNull(metric) }.takeIf { it.isNotEmpty() }?.average()
}
object CycleReview {
    fun build(starts: List<Long>, samples: List<ReviewSample>, today: Long): List<ReviewCycle> {
        val ordered = starts.filter { it <= today }.distinct().sorted()
        val logged = samples.filter { it.day <= today }.distinctBy { it.day }.filter {
            it.levels.size == 4 && it.levels.all { n -> n in 0..3 }
        }
        return ordered.mapIndexed { i, start ->
            val next = ordered.getOrNull(i + 1)
            val end = next ?: (today + 1)
            ReviewCycle(start, end, next != null, logged.filter { it.day >= start && it.day < end }.sortedBy { it.day })
        }.asReversed().take(6)
    }
}
