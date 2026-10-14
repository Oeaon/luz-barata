package com.luzbarata.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.luzbarata.app.AppContainer
import com.luzbarata.app.LuzBarataApp
import com.luzbarata.app.data.DayResult
import com.luzbarata.app.data.SPAIN_ZONE
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class TomorrowPricesWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as LuzBarataApp).container
        val settings = container.settings.settings.first()
        val tomorrow = LocalDate.now(SPAIN_ZONE).plusDays(1)
        if (!settings.notifyTomorrow || settings.lastNotifiedDate == tomorrow) return finish(container)

        val result = container.prices.day(tomorrow, settings.zone, refresh = true)
        if (result is DayResult.Available && result.prices.isComplete) {
            TomorrowPricesNotifier.show(applicationContext, result.prices, settings.windowHours)
            container.settings.setLastNotified(tomorrow)
            return finish(container)
        }
        return if (runAttemptCount < MAX_RETRIES) Result.retry() else finish(container)
    }

    private fun finish(container: AppContainer): Result {
        container.scheduler.scheduleNext()
        return Result.success()
    }

    private companion object {
        const val MAX_RETRIES = 6
    }
}
