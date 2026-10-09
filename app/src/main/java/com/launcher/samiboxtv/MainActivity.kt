package com.launcher.samiboxtv

import android.annotation.SuppressLint
import android.content.Intent
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
import com.launcher.samiboxtv.util.TvRemoteKeyCodes
import com.launcher.samiboxtv.util.iptv.IptvPlaylistManager

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory((application as SamiBoxApplication).container)
    }

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IptvPlaylistManager.init(applicationContext)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Si el usuario pulsa HOME, cerrar reproductor IPTV y reproductor de medios inmediatamente
        viewModel.onEvent(HomeUiEvent.CloseLiveTv)
        viewModel.onEvent(HomeUiEvent.CloseMediaPlayer)
        viewModel.onEvent(HomeUiEvent.CloseSettings)
        viewModel.onEvent(HomeUiEvent.CloseContextMenu)
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

            // 2. Teclas físicas del control remoto y accesos directos
            val code = event.keyCode
            val scan = event.scanCode

            when {
                code == KeyEvent.KEYCODE_SETTINGS || code == TvRemoteKeyCodes.KEY_EXTRA_MENU || scan == TvRemoteKeyCodes.KEY_EXTRA_MENU -> {
                    viewModel.onEvent(HomeUiEvent.OpenSettings)
                    return true
                }
                code == KeyEvent.KEYCODE_MENU -> {
                    if (!viewModel.uiState.value.isIptvPlayerOpen) {
                        viewModel.onEvent(HomeUiEvent.OpenSettings)
                        return true
                    }
                    // Si el reproductor IPTV está abierto, dejamos que KEYCODE_MENU pase a Compose
                }
                code == TvRemoteKeyCodes.KEY_CH_UP || scan == TvRemoteKeyCodes.KEY_CH_UP || code == KeyEvent.KEYCODE_CHANNEL_UP -> {
                    if (viewModel.uiState.value.isIptvPlayerOpen) {
                        viewModel.onEvent(HomeUiEvent.NextChannel)
                    } else if (viewModel.uiState.value.iptvChannels.isNotEmpty()) {
                        viewModel.onEvent(HomeUiEvent.OpenLiveTv)
                    }
                    return true
                }
                code == TvRemoteKeyCodes.KEY_CH_DOWN || scan == TvRemoteKeyCodes.KEY_CH_DOWN || code == KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                    if (viewModel.uiState.value.isIptvPlayerOpen) {
                        viewModel.onEvent(HomeUiEvent.PreviousChannel)
                    } else if (viewModel.uiState.value.iptvChannels.isNotEmpty()) {
                        viewModel.onEvent(HomeUiEvent.OpenLiveTv)
                    }
                    return true
                }
                code == TvRemoteKeyCodes.KEY_EPG_GUIDE || scan == TvRemoteKeyCodes.KEY_EPG_GUIDE || code == KeyEvent.KEYCODE_GUIDE -> {
                    if (!viewModel.uiState.value.isIptvPlayerOpen && viewModel.uiState.value.iptvChannels.isNotEmpty()) {
                        viewModel.onEvent(HomeUiEvent.OpenLiveTv)
                    }
                    viewModel.onEvent(HomeUiEvent.ToggleChannelList)
                    return true
                }
                code == TvRemoteKeyCodes.KEY_INFO || scan == TvRemoteKeyCodes.KEY_INFO || code == KeyEvent.KEYCODE_INFO -> {
                    viewModel.onEvent(HomeUiEvent.ToggleIptvOsd)
                    return true
                }
                TvRemoteKeyCodes.isFavoriteKey(code, scan) -> {
                    if (viewModel.uiState.value.isIptvPlayerOpen) {
                        if (!viewModel.uiState.value.isIptvChannelListOpen) {
                            viewModel.onEvent(HomeUiEvent.ToggleCurrentIptvFavorite)
                            return true
                        }
                        // Si la guía lateral de canales está abierta, dejamos pasar el evento
                        // a Compose para que ChannelDrawerItem marque/desmarque el canal enfocado
                    }
                }
                code == TvRemoteKeyCodes.KEY_LIVE_TV || scan == TvRemoteKeyCodes.KEY_LIVE_TV || code == KeyEvent.KEYCODE_TV -> {
                    if (viewModel.uiState.value.iptvChannels.isNotEmpty()) {
                        viewModel.onEvent(HomeUiEvent.OpenLiveTv)
                    }
                    return true
                }
            }
        }

        // Permitir que KEYCODE_BACK y el D-Pad lleguen intactos a Compose
        return super.dispatchKeyEvent(event)
    }
}