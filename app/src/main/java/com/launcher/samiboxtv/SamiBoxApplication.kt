package com.launcher.samiboxtv

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import com.launcher.samiboxtv.di.AppContainer
import com.launcher.samiboxtv.di.DefaultAppContainer

/**
 * Clase Application principal que inicializa el contenedor de dependencias (DI)
 * y configura Coil con límites de memoria RAM optimizados para dispositivos TV.
 */
class SamiBoxApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    // Reserva máximo 15% de RAM disponible para caché de iconos y banners
                    .maxSizePercent(0.15)
                    .build()
            }
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .allowHardware(true)
            .crossfade(false)
            .build()
    }
}
