package com.launcher.samiboxtv

import android.view.KeyEvent
import com.launcher.samiboxtv.util.TvRemoteKeyCodes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvRemoteKeyCodesTest {

    @Test
    fun isFavoriteKey_recognizesFavoriteCodes() {
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(TvRemoteKeyCodes.KEY_FAV_TOGGLE))
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(0, TvRemoteKeyCodes.KEY_FAV_TOGGLE))
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(TvRemoteKeyCodes.KEY_FAV_CODE_208))
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(0, TvRemoteKeyCodes.SCAN_KEY_FAVORITES_LINUX))
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_PROG_YELLOW))
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_BOOKMARK))
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_STAR))
        assertTrue(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_BUTTON_Y))
    }

    @Test
    fun isFavoriteKey_rejectsNormalKeys() {
        assertFalse(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_DPAD_CENTER))
        assertFalse(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_BACK))
        assertFalse(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_HOME))
        assertFalse(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_CHANNEL_UP))
        assertFalse(TvRemoteKeyCodes.isFavoriteKey(KeyEvent.KEYCODE_CHANNEL_DOWN))
    }
}
