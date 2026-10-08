package com.launcher.samiboxtv

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
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
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU,
                KeyEvent.KEYCODE_SETTINGS -> {
                    viewModel.onEvent(com.launcher.samiboxtv.presentation.home.HomeUiEvent.OpenSettings)
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    val state = viewModel.uiState.value
                    when {
                        state.isSettingsOpen -> {
                            viewModel.onEvent(com.launcher.samiboxtv.presentation.home.HomeUiEvent.CloseSettings)
                            return true
                        }
                        state.isSystemLogOpen -> {
                            viewModel.onEvent(com.launcher.samiboxtv.presentation.home.HomeUiEvent.CloseSystemLog)
                            return true
                        }
                        state.isAddDialogOpen -> {
                            viewModel.onEvent(com.launcher.samiboxtv.presentation.home.HomeUiEvent.CloseAddDialog)
                            return true
                        }
                        state.selectedAppForMenu != null -> {
                            viewModel.onEvent(com.launcher.samiboxtv.presentation.home.HomeUiEvent.CloseContextMenu)
                            return true
                        }
                        else -> {
                            // En la pantalla principal de un Launcher TV, la tecla BACK no debe cerrar la app
                            return true
                        }
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}