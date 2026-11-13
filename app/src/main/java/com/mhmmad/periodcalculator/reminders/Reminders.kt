package com.mhmmad.periodcalculator.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.mhmmad.periodcalculator.R
import com.mhmmad.periodcalculator.data.PeriodRepository
import com.mhmmad.periodcalculator.data.PreferenceRepository
import com.mhmmad.periodcalculator.domain.CycleEngine
import com.mhmmad.periodcalculator.domain.PeriodRecord
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/** No health or period content appears in a notification. */
class GenericReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val config = PreferenceRepository(context).settings.first()
        if (!config.reminder && !config.periodReminder) return Result.success()
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val nearPeriod = if (!config.periodReminder) false else {
            val periods = PeriodRepository(context).periods.first()
            val prediction = CycleEngine.predict(periods.map {
                PeriodRecord(it.id, LocalDate.ofEpochDay(it.startEpochDay), it.endEpochDay?.let(LocalDate::ofEpochDay))
            }, config.cycleLength, LocalDate.now())
            // With limited confidence, do not imply a definite event or notify too early.
            prediction != null && !prediction.isExtrapolated &&
                prediction.confidence != com.mhmmad.periodcalculator.domain.Confidence.LOW &&
                CycleEngine.numberOfDaysUntil(prediction.nextStart, LocalDate.now()) in 0..3
        }
        if (!config.reminder && !nearPeriod) return Result.success()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(
            NotificationChannel("general", context.getString(R.string.reminders), NotificationManager.IMPORTANCE_DEFAULT)
        )
        val n = NotificationCompat.Builder(context, "general")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true).build()
        NotificationManagerCompat.from(context).notify(8111, n)
        return Result.success()
    }
}
object ReminderScheduler {
    private const val WORK = "generic_journal_reminder"
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<GenericReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
    fun cancel(context: Context) { WorkManager.getInstance(context).cancelUniqueWork(WORK) }
}
