package com.launcher.samiboxtv

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeViewModel
import com.launcher.samiboxtv.presentation.theme.SamiBoxTVTheme
import com.launcher.samiboxtv.util.DevLogManager

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory((application as SamiBoxApplication).container)
    }

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            // 1. Registro para el servidor de depuración (captura todo sin bloquear)
            val keyName = KeyEvent.keyCodeToString(event.keyCode)
            val logMsg = "Botón: $keyName | KeyCode: ${event.keyCode} | ScanCode: ${event.scanCode}"
            DevLogManager.log("KEY", logMsg)

            if (viewModel.uiState.value.showKeyDebugToast) {
                Toast.makeText(this, logMsg, Toast.LENGTH_SHORT).show()
            }

            // 2. Teclas específicas no estándar (MENU / AJUSTES)
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU,
                KeyEvent.KEYCODE_SETTINGS -> {
                    viewModel.onEvent(HomeUiEvent.OpenSettings)
                    return true
                }
            }
        }

        // Permitir que KEYCODE_BACK y el D-Pad lleguen intactos a Compose
        return super.dispatchKeyEvent(event)
    }
}