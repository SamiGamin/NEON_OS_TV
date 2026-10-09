package com.launcher.samiboxtv.util

import android.view.KeyEvent

/**
 * Códigos de teclas físicas del control remoto de Android TV Box.
 */
object TvRemoteKeyCodes {
    const val KEY_CH_UP = 166
    const val KEY_CH_DOWN = 167
    const val KEY_INFO = 165
    const val KEY_FAV_TOGGLE = 288
    const val KEY_EPG_GUIDE = 4056
    const val KEY_LIVE_TV = 4020
    const val KEY_EXTRA_MENU = 4077

    // Códigos estándar de Android TV y variantes de fabricantes para FAVORITOS
    const val KEY_FAV_CODE_208 = 208                        // Código OEM común en TV Boxes para FAV
    const val KEY_FAV_YELLOW = KeyEvent.KEYCODE_PROG_YELLOW   // 185 (Botón amarillo DVB)
    const val KEY_FAV_BOOKMARK = KeyEvent.KEYCODE_BOOKMARK   // 174 (Marcapáginas/Favoritos)
    const val KEY_FAV_STAR = KeyEvent.KEYCODE_STAR           // 17  (Estrella)
    const val KEY_FAV_BUTTON_Y = KeyEvent.KEYCODE_BUTTON_Y   // 100 (Botón Y)
    const val KEY_FAV_BLUE = KeyEvent.KEYCODE_PROG_BLUE     // 186 (Botón azul)
    const val SCAN_KEY_FAVORITES_LINUX = 364                 // Linux kernel KEY_FAVORITES

    /**
     * Determina si un evento de teclado físico corresponde a la acción de FAVORITOS
     * evaluando tanto el KeyCode de Android como el ScanCode emitido por el hardware.
     */
    fun isFavoriteKey(keyCode: Int, scanCode: Int = 0): Boolean {
        return keyCode == KEY_FAV_TOGGLE ||
               scanCode == KEY_FAV_TOGGLE ||
               keyCode == KEY_FAV_CODE_208 ||
               scanCode == KEY_FAV_CODE_208 ||
               scanCode == SCAN_KEY_FAVORITES_LINUX ||
               keyCode == KeyEvent.KEYCODE_PROG_YELLOW ||
               keyCode == KeyEvent.KEYCODE_BOOKMARK ||
               keyCode == KeyEvent.KEYCODE_STAR ||
               keyCode == KeyEvent.KEYCODE_BUTTON_Y
    }
}
