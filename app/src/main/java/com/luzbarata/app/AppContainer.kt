package com.luzbarata.app

import android.content.Context
import com.luzbarata.app.data.PriceCache
import com.luzbarata.app.data.PvpcApi
import com.luzbarata.app.data.PvpcRepository
import com.luzbarata.app.data.SettingsRepository
import com.luzbarata.app.data.settingsDataStore
import com.luzbarata.app.notifications.NotificationScheduler
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/** Dependencias compartidas de la app, creadas una vez en [LuzBarataApp]. */
class AppContainer(context: Context) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val settings = SettingsRepository(context.settingsDataStore)
    val prices = PvpcRepository(PvpcApi(httpClient), PriceCache(File(context.filesDir, "prices")))
    val scheduler = NotificationScheduler(context.applicationContext)
}
