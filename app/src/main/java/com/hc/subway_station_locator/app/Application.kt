package com.hc.subway_station_locator.app

import android.app.Application
import android.content.Context

class Application: Application() {

    companion object {
        private lateinit var instance: Application

        val context: Context get() = instance.applicationContext
    }

    init{
        instance = this
    }
}