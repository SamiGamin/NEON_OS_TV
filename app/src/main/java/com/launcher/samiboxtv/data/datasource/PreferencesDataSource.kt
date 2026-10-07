package com.launcher.samiboxtv.data.datasource

import android.content.Context
import android.content.SharedPreferences

/**
 * Fuente de datos de preferencias locales para persistencia de configuración del Launcher.
 */
interface PreferencesDataSource {
    fun getHiddenApps(): Set<String>
    fun addHiddenApp(packageName: String)
    fun removeHiddenApp(packageName: String)
    fun setHiddenApps(packageNames: Set<String>)
    fun toggleHiddenApp(packageName: String): Boolean
    fun getFavoriteApps(): Set<String>
    fun toggleFavoriteApp(packageName: String): Boolean
    fun getCustomOrder(): List<String>
    fun saveCustomOrder(order: List<String>)
}

class PreferencesDataSourceImpl(
    context: Context
) : PreferencesDataSource {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN_APPS, emptySet()) ?: emptySet()
    }

    override fun addHiddenApp(packageName: String) {
        val hidden = getHiddenApps().toMutableSet()
        hidden.add(packageName)
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hidden).apply()
    }

    override fun removeHiddenApp(packageName: String) {
        val hidden = getHiddenApps().toMutableSet()
        hidden.remove(packageName)
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hidden).apply()
    }

    override fun setHiddenApps(packageNames: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, packageNames).apply()
    }

    override fun toggleHiddenApp(packageName: String): Boolean {
        val hidden = getHiddenApps().toMutableSet()
        val willBeHidden = if (hidden.contains(packageName)) {
            hidden.remove(packageName)
            false
        } else {
            hidden.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hidden).apply()
        return willBeHidden
    }

    override fun getFavoriteApps(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_APPS, emptySet()) ?: emptySet()
    }

    override fun toggleFavoriteApp(packageName: String): Boolean {
        val favs = getFavoriteApps().toMutableSet()
        val isNowFav = if (favs.contains(packageName)) {
            favs.remove(packageName)
            false
        } else {
            favs.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITE_APPS, favs).apply()
        return isNowFav
    }

    override fun getCustomOrder(): List<String> {
        val orderStr = prefs.getString(KEY_CUSTOM_ORDER, "") ?: ""
        return if (orderStr.isEmpty()) emptyList() else orderStr.split(",")
    }

    override fun saveCustomOrder(order: List<String>) {
        prefs.edit().putString(KEY_CUSTOM_ORDER, order.joinToString(",")).apply()
    }

    companion object {
        private const val PREFS_NAME = "samibox_prefs"
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_FAVORITE_APPS = "favorite_apps"
        private const val KEY_CUSTOM_ORDER = "custom_order"
    }
}
