package com.example.fuelcalculator

import android.app.Application
import timber.log.Timber

class FuelCalculatorApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}