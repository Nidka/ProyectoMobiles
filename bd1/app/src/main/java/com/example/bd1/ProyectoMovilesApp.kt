package com.example.bd1

import android.app.Application
import com.example.bd1.di.AppContainer

class ProyectoMovilesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}
