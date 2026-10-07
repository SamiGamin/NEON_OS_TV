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
        private const val KEY_CUSTOM_ORDER = "custom_order"
    }
}
