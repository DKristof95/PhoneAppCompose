package com.wozavez.fmr.phoneappcompose

import android.app.Application
import timber.log.Timber

class PhoneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}