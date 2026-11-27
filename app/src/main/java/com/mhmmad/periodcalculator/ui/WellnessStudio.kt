package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mhmmad.periodcalculator.data.DailyEntryEntity
import java.time.LocalDate

@Composable
fun WellnessStudio(entries: List<DailyEntryEntity>) {
    val context = LocalContext.current
    val lang = androidx.core.os.ConfigurationCompat.getLocales(context.resources.configuration).get(0)?.language
    fun t(en: String, ar: String, pt: String) = when(lang) { "ar" -> ar; "pt" -> pt; else -> en }
    val today = LocalDate.now()
    var window by remember { mutableIntStateOf(30) }
    var metric by remember { mutableIntStateOf(0) }
    val labels = listOf(t("Cramps","التقلصات","Cólicas"), t("Headache","الصداع","Dor de cabeça"), t("Fatigue","الإرهاق","Fadiga"), t("Bloating","الانتفاخ","Inchaço"))
    val values = entries.filter { it.epochDay in today.minusDays((window-1).toLong()).toEpochDay()..today.toEpochDay() }
    fun score(e: DailyEntryEntity) = when(metric) { 0 -> e.cramps; 1 -> e.headache; 2 -> e.fatigue; else -> e.bloating }
    val ink = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    ScreenColumn {
        Panel {
            Text(t("Wellness atlas","أطلس العافية","Atlas de bem-estar"), style = MaterialTheme.typography.headlineMedium)
            Text(t("Explore your recorded patterns, not medical predictions.","استكشفي أنماط سجلاتك، وليس تنبؤات طبية.","Explore seus registros, não previsões médicas."))
            Canvas(Modifier.fillMaxWidth().height(100.dp)) {
                val stroke = 10.dp.toPx()
                val r = size.height * .36f
                drawCircle(track, r, center, style = Stroke(stroke))
                val diameter = r*2
                drawArc(ink, -90f, 360f*(values.size.toFloat()/window).coerceIn(0f,1f), false, topLeft = androidx.compose.ui.geometry.Offset(center.x-r,center.y-r), size = androidx.compose.ui.geometry.Size(diameter,diameter), style = Stroke(stroke,cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30,90).forEach { n -> FilterChip(selected = window == n, onClick = { window = n }, label = { Text("$n "+t("days","يومًا","dias")) }) }
            }
            Text("${values.size}/$window "+t("days recorded","أيام مسجلة","dias registrados"))
            LinearProgressIndicator(progress = { (values.size.toFloat()/window).coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth())
        }
        Panel {
            Subheading(t("Symptom intensity map","خريطة شدة الأعراض","Mapa de intensidade"))
            labels.chunked(2).forEachIndexed { row, pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { pair.forEachIndexed { col, label ->
                    val index = row*2+col
                    FilterChip(selected = metric == index, onClick = { metric = index }, label = { Text(label) })
                } }
            }
            val byDay = values.associateBy { it.epochDay }
            val days = (0 until window).map { today.minusDays((window-1-it).toLong()).toEpochDay() }
            days.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { day ->
                        val entry = byDay[day]
                        val color = if(entry == null) track else ink.copy(alpha = .18f + .26f*score(entry).coerceIn(0,3))
                        Surface(Modifier.weight(1f).height(28.dp), color = color, shape = MaterialTheme.shapes.small) {
                            Box(contentAlignment = Alignment.Center) { Text(LocalDate.ofEpochDay(day).dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall, color = if(entry != null && score(entry)>=2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) }
                        }
                    }
                    repeat(7-week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text(t("Neutral cells: no record. Darker colour: greater recorded intensity.","الرمادي الفاتح: لا سجل. اللون الأغمق: شدة مسجلة أعلى.","Cinza claro: sem registro. Cor mais escura: maior intensidade registrada."))
            if(values.isNotEmpty()) Text(t("Days with high intensity: ","أيام الشدة العالية: ","Dias de intensidade alta: ")+values.count { score(it)>=3 })
        }
        Panel {
            Subheading(t("Prepare for a consultation","الاستعداد للاستشارة","Prepare-se para uma consulta"))
            Text(t("Note when symptoms began, how they affect daily life and whether bleeding patterns changed. Seek medical advice if pain or bleeding disrupts your normal activities.","دوّني بداية الأعراض وتأثيرها على حياتك وأي تغير في النزف. استشيري الطبيب إذا أعاق الألم أو النزف نشاطك المعتاد.","Anote quando os sintomas começaram, seu impacto na rotina e mudanças no sangramento. Procure orientação médica se a dor ou o sangramento interferir nas atividades."))
            Text(t("Source: NHS – Period problems. Educational only.","المصدر: NHS – مشكلات الدورة. للتثقيف فقط.","Fonte: NHS – Problemas menstruais. Apenas educativo."), style = MaterialTheme.typography.bodySmall)
        }
    }
}
