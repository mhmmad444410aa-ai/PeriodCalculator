package com.mhmmad.periodcalculator.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mhmmad.periodcalculator.data.PeriodEntity
import com.mhmmad.periodcalculator.data.DailyEntryEntity
import java.io.ByteArrayOutputStream
import java.time.LocalDate

data class ExportCommands(val csv: () -> Unit, val pdf: () -> Unit)

@Composable
fun ExportActions(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, content: @Composable (ExportCommands) -> Unit) {
    val context = LocalContext.current
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) write(context, uri, Exporter.toCsv(periods, entries).toByteArray(Charsets.UTF_8))
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) write(context, uri, Exporter.toPdf(periods, entries))
    }
    content(ExportCommands({ csvLauncher.launch("period-report.csv") }, { pdfLauncher.launch("period-report.pdf") }))
}
private fun write(context: Context, uri: Uri, bytes: ByteArray) {
    runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } }
}
object Exporter {
    private fun csvSafe(value: String): String {
        val neutral = if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'" + value else value
        return "\"${neutral.replace("\"", "\"\"") }\""
    }
    fun toCsv(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>): String = buildString {
        appendLine("record_type,date,end_date,flow,cramps,headache,bloating,fatigue,mood,sleep_hours,note")
        periods.forEach { p -> appendLine(listOf("period", LocalDate.ofEpochDay(p.startEpochDay),
            p.endEpochDay?.let(LocalDate::ofEpochDay) ?: "", p.flowIntensity, "", "", "", "", "", "", p.note)
            .joinToString(",") { csvSafe(it.toString()) }) }
        entries.forEach { e -> appendLine(listOf("daily", LocalDate.ofEpochDay(e.epochDay), "", "",
            e.cramps, e.headache, e.bloating, e.fatigue, e.mood, e.sleepHours, e.notes)
            .joinToString(",") { csvSafe(it.toString()) }) }
    }
    fun toPdf(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>): ByteArray {
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(56, 37, 63); textSize = 12f }
        val title = Paint(paint).apply { textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        var page = 1
        var y = 85f
        var current = document.startPage(PdfDocument.PageInfo.Builder(595, 842, page).create())
        current.canvas.drawText("Period Calculator - local health journal", 36f, 48f, title)
        val lines = mutableListOf("Period records:")
        periods.forEach { p -> lines += "${LocalDate.ofEpochDay(p.startEpochDay)} - ${p.endEpochDay?.let(LocalDate::ofEpochDay) ?: "ongoing"}    Flow: ${p.flowIntensity}" }
        lines += "Daily symptom logs: ${entries.size}"
        entries.forEach { e -> lines += "${LocalDate.ofEpochDay(e.epochDay)}  C:${e.cramps} H:${e.headache} B:${e.bloating} F:${e.fatigue} M:${e.mood}" }
        lines.forEach { line ->
            if (y >= 795f) {
                document.finishPage(current); page++; y = 55f
                current = document.startPage(PdfDocument.PageInfo.Builder(595, 842, page).create())
            }
            current.canvas.drawText(line.take(95), 36f, y, paint); y += 22f
        }
        document.finishPage(current)
        return ByteArrayOutputStream().use { stream -> document.writeTo(stream); document.close(); stream.toByteArray() }
    }
}
