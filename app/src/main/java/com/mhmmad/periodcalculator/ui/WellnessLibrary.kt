package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mhmmad.periodcalculator.R

@Composable
fun wellnessLanguage(): String = androidx.core.os.ConfigurationCompat.getLocales(LocalContext.current.resources.configuration).get(0)?.language ?: "en"

private fun translated(lang: String, en: String, ar: String, pt: String) = when(lang) { "ar" -> ar; "pt" -> pt; else -> en }

@Composable
fun WellnessGateway(onAtlas: () -> Unit, onLibrary: () -> Unit) {
    val lang = wellnessLanguage()
    fun t(en: String, ar: String, pt: String) = translated(lang,en,ar,pt)
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(c.primaryContainer,c.surface,c.secondaryContainer))).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.lucide_flower), null, Modifier.size(48.dp))
            Column(Modifier.weight(1f)) {
                Text(t("Your wellness space","مساحتك الصحية","Seu espaço de bem-estar"), style = MaterialTheme.typography.headlineSmall, color = c.onSurface)
                Text(t("Discover patterns and learn","اكتشفي الأنماط وتعلمي","Descubra padrões e aprenda"), style = MaterialTheme.typography.bodyMedium, color = c.onSurface)
            }
        }
        Button(onClick = onAtlas, modifier = Modifier.fillMaxWidth()) { Text(t("Explore symptom atlas","استكشفي أطلس الأعراض","Explorar atlas de sintomas")) }
        OutlinedButton(onClick = onLibrary, modifier = Modifier.fillMaxWidth()) { Text(t("Health library","المكتبة الصحية","Biblioteca de saúde")) }
    }
}

private data class HealthArticle(val title: String, val summary: String, val url: String)

@Composable
fun WellnessLibrary() {
    val lang = wellnessLanguage()
    fun t(en: String, ar: String, pt: String) = translated(lang,en,ar,pt)
    val uri = androidx.compose.ui.platform.LocalUriHandler.current
    val articles = listOf(
        HealthArticle(t("Understanding periods","فهم الدورة الشهرية","Entendendo a menstruação"),
            t("The first day of bleeding marks the beginning of a menstrual cycle. Logging start dates helps describe your own pattern.","اليوم الأول للنزف هو بداية الدورة الشهرية. تسجيل تواريخ البداية يساعد على وصف نمطك الشخصي.","O primeiro dia de sangramento inicia o ciclo menstrual. Registrar as datas ajuda a conhecer seu padrão."),
            "https://www.nhs.uk/conditions/periods/"),
        HealthArticle(t("Period pain","ألم الدورة","Dor menstrual"),
            t("Gentle movement or a warm compress may help ease discomfort. Seek advice when pain becomes severe or changes from your usual pattern.","قد تساعد الحركة الخفيفة أو الكمادات الدافئة في تخفيف الانزعاج. استشيري الطبيب عند الألم الشديد أو تغير نمطه المعتاد.","Movimentos leves ou compressas mornas podem aliviar o desconforto. Procure orientação se a dor for intensa ou mudar de padrão."),
            "https://www.nhs.uk/symptoms/period-pain/"),
        HealthArticle(t("Heavy bleeding","غزارة النزف","Sangramento intenso"),
            t("Bleeding that interferes with your routine deserves medical attention. Keep a record of how long it lasts and its impact.","النزف الذي يعوق أنشطتك اليومية يستحق تقييمًا طبيًا. سجلي مدته وتأثيره على حياتك.","Sangramento que interfere na rotina merece avaliação médica. Registre a duração e o impacto."),
            "https://www.nhs.uk/conditions/heavy-periods/"),
        HealthArticle(t("Irregular cycles","عدم انتظام الدورة","Ciclos irregulares"),
            t("Cycle timing can vary. Discuss persistent changes or accompanying symptoms with a health professional.","قد تختلف مواعيد الدورة. ناقشي التغيرات المستمرة أو الأعراض المصاحبة مع مختص صحي.","A duração do ciclo pode variar. Converse com um profissional sobre mudanças persistentes ou outros sintomas."),
            "https://www.nhs.uk/symptoms/irregular-periods/")
    )
    var selected by remember { mutableIntStateOf(-1) }
    ScreenColumn {
        Panel {
            Image(painterResource(R.drawable.lucide_flower), null, Modifier.size(54.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary))
            Text(t("Health library","المكتبة الصحية","Biblioteca de saúde"), style = MaterialTheme.typography.headlineMedium)
            Text(t("Four practical guides, available offline. Each includes its original medical source.","أربعة أدلة عملية تعمل دون إنترنت، مع رابط المصدر الطبي لكل دليل.","Quatro guias práticos disponíveis offline, com suas fontes médicas."))
        }
        articles.forEachIndexed { index, article ->
            Panel {
                Text(article.title, style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { selected = if(selected == index) -1 else index }) { Text(if(selected == index) t("Close","إغلاق","Fechar") else t("Read guide","قراءة الدليل","Ler guia")) }
                if(selected == index) {
                    Text(article.summary)
                    OutlinedButton(onClick = { runCatching { uri.openUri(article.url) } }, modifier = Modifier.fillMaxWidth()) { Text(t("Original NHS source (English)","المصدر الطبي الأصلي (الإنجليزية)","Fonte NHS original (inglês)")) }
                }
            }
        }
        Notice(t("Educational information only; not a diagnosis or a substitute for medical care.","معلومات تثقيفية فقط، لا تُعد تشخيصًا ولا بديلًا للرعاية الطبية.","Apenas informação educativa, não substitui avaliação médica."))
        Text("Illustration: Lucide Icons (ISC)", style = MaterialTheme.typography.labelSmall)
    }
}
