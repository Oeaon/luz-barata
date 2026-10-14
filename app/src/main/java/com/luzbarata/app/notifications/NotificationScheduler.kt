package com.luzbarata.app.notifications

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.luzbarata.app.data.SPAIN_ZONE
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Programa un aviso diario cuando REE publica los precios de mañana (hacia las 20:15).
 * Es un trabajo de una vez que se vuelve a programar al terminar, para que no se desplace
 * como un trabajo periódico tras los reintentos.
 */
class NotificationScheduler(private val context: Context) {
    /** Al abrir la app: no toca lo que ya esté programado. */
    fun ensureScheduled() = enqueue(ExistingWorkPolicy.KEEP, allowTonight = true)

    /** Al activar los avisos: si ya han salido los precios, avisa esta misma noche. */
    fun onNotificationsEnabled() = enqueue(ExistingWorkPolicy.REPLACE, allowTonight = true)

    /** Desde el propio worker al terminar: la siguiente ejecución es mañana. */
    fun scheduleNext() = enqueue(ExistingWorkPolicy.APPEND_OR_REPLACE, allowTonight = false)

    private fun enqueue(policy: ExistingWorkPolicy, allowTonight: Boolean) {
        val request = OneTimeWorkRequestBuilder<TomorrowPricesWorker>()
            .setInitialDelay(delayUntilNextRun(ZonedDateTime.now(SPAIN_ZONE), allowTonight).toMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.LINEAR, RETRY_MINUTES, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
    }

    companion object {
        private const val WORK_NAME = "tomorrow_prices"
        private const val RETRY_MINUTES = 30L
        private val RUN_AT: LocalTime = LocalTime.of(20, 30)

        fun delayUntilNextRun(now: ZonedDateTime, allowTonight: Boolean): Duration {
            if (allowTonight && !now.toLocalTime().isBefore(RUN_AT)) return Duration.ZERO
            var next = now.with(RUN_AT)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return Duration.between(now, next)
        }
    }
}
