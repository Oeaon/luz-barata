package com.luzbarata.app

import android.app.Application
import com.luzbarata.app.notifications.TomorrowPricesNotifier

class LuzBarataApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        TomorrowPricesNotifier.createChannel(this)
        container.scheduler.ensureScheduled()
    }
}
