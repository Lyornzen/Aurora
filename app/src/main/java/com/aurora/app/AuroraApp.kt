package com.aurora.app

import android.app.Application
import timber.log.Timber

class AuroraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
