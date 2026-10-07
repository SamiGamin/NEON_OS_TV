package com.launcher.samiboxtv

import android.app.Application
import com.launcher.samiboxtv.di.AppContainer
import com.launcher.samiboxtv.di.DefaultAppContainer

/**
 * Clase Application principal que inicializa el contenedor de dependencias (DI).
 */
class SamiBoxApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
