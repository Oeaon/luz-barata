package com.luzbarata.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.luzbarata.app.MainActivity
import com.luzbarata.app.R
import com.luzbarata.app.ui.formatHour
import com.luzbarata.app.ui.formatPrice
import com.luzbarata.app.ui.formatRange
import com.luzbarata.core.DayPrices

object TomorrowPricesNotifier {
    const val EXTRA_OPEN_TOMORROW = "open_tomorrow"
    private const val CHANNEL_ID = "tomorrow_prices"
    private const val NOTIFICATION_ID = 1

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_tomorrow_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.channel_tomorrow_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // comprobado en canNotify
    fun show(context: Context, prices: DayPrices, windowHours: Int) {
        if (!canNotify(context)) return
        val window = prices.cheapestWindow(windowHours) ?: return
        val expensive = prices.mostExpensive
        val text = context.getString(
            R.string.notification_tomorrow_text,
            formatRange(window.start, window.end),
            formatPrice(window.average),
            formatHour(expensive.start),
            formatPrice(expensive.eurPerKwh),
        )
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN_TOMORROW, true)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_bolt)
            .setContentTitle(context.getString(R.string.notification_tomorrow_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
