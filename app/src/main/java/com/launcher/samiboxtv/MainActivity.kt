package com.launcher.samiboxtv

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.launcher.samiboxtv.di.ViewModelFactory
import com.launcher.samiboxtv.presentation.home.HomeScreen
import com.launcher.samiboxtv.presentation.home.HomeViewModel
import com.launcher.samiboxtv.presentation.theme.SamiBoxTVTheme
import com.launcher.samiboxtv.services.monitor.MemoryUsageTester
import com.launcher.samiboxtv.services.overlay.OverlayWindowManager

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory((application as SamiBoxApplication).container)
    }

    private lateinit var memoryTester: MemoryUsageTester
    private lateinit var overlayManager: OverlayWindowManager

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        memoryTester = MemoryUsageTester(this)
        memoryTester.startMonitoring(intervalMillis = 10000)

        overlayManager = OverlayWindowManager(applicationContext)

        if (!overlayManager.canDrawOverlays()) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        setContent {
            SamiBoxTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        memoryTester.stopMonitoring()
        overlayManager.hide()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU -> {
                    if (overlayManager.canDrawOverlays()) {
                        overlayManager.toggle()
                    } else {
                        Toast.makeText(
                            this,
                            "Activa el permiso de superposición en Ajustes",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    if (overlayManager.isVisible()) {
                        overlayManager.hide()
                        return true
                    }
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}