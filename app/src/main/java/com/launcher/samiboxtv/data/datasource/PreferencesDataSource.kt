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
    fun getCardStyle(): String
    fun setCardStyle(styleName: String)
    fun getCustomCategories(): List<String>
    fun addCustomCategory(categoryName: String): Boolean
    fun removeCustomCategory(categoryName: String): Boolean
    fun getAppCategoryMap(): Map<String, String>
    fun setAppCategory(packageName: String, categoryName: String)
    fun getShowAppNames(): Boolean
    fun setShowAppNames(show: Boolean)
    fun getFavoriteIptvChannels(): Set<String>
    fun saveFavoriteIptvChannels(channels: Set<String>)
    fun getAppLayoutMode(): String
    fun setAppLayoutMode(mode: String)
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

    override fun getCardStyle(): String {
        return prefs.getString(KEY_CARD_STYLE, "BANNER_16_9") ?: "BANNER_16_9"
    }

    override fun setCardStyle(styleName: String) {
        prefs.edit().putString(KEY_CARD_STYLE, styleName).apply()
    }

    override fun getCustomCategories(): List<String> {
        val stored = prefs.getString(KEY_CUSTOM_CATEGORIES, null)
        if (stored.isNullOrBlank()) {
            return listOf("STREAMING", "GAMING", "APPS")
        }
        return stored.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    override fun addCustomCategory(categoryName: String): Boolean {
        val current = getCustomCategories().toMutableList()
        val formatted = categoryName.trim().uppercase()
        if (formatted.isBlank() || current.contains(formatted)) return false
        current.add(formatted)
        prefs.edit().putString(KEY_CUSTOM_CATEGORIES, current.joinToString(",")).apply()
        return true
    }

    override fun removeCustomCategory(categoryName: String): Boolean {
        val current = getCustomCategories().toMutableList()
        val formatted = categoryName.trim().uppercase()
        val removed = current.removeAll { it.equals(formatted, ignoreCase = true) }
        if (removed) {
            prefs.edit().putString(KEY_CUSTOM_CATEGORIES, current.joinToString(",")).apply()
            val appMap = getAppCategoryMap().toMutableMap()
            var modified = false
            val iterator = appMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.equals(formatted, ignoreCase = true)) {
                    iterator.remove()
                    modified = true
                }
            }
            if (modified) {
                val json = org.json.JSONObject(appMap as Map<*, *>)
                prefs.edit().putString(KEY_APP_CATEGORIES, json.toString()).apply()
            }
        }
        return removed
    }

    override fun getAppCategoryMap(): Map<String, String> {
        val raw = prefs.getString(KEY_APP_CATEGORIES, null) ?: return emptyMap()
        val map = mutableMapOf<String, String>()
        try {
            val json = org.json.JSONObject(raw)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.getString(key)
            }
        } catch (_: Exception) {}
        return map
    }

    override fun setAppCategory(packageName: String, categoryName: String) {
        val map = getAppCategoryMap().toMutableMap()
        if (categoryName.isBlank()) {
            map.remove(packageName)
        } else {
            map[packageName] = categoryName.trim().uppercase()
        }
        val json = org.json.JSONObject(map as Map<*, *>)
        prefs.edit().putString(KEY_APP_CATEGORIES, json.toString()).apply()
    }

    override fun getShowAppNames(): Boolean {
        return prefs.getBoolean(KEY_SHOW_APP_NAMES, true)
    }

    override fun setShowAppNames(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_APP_NAMES, show).apply()
    }

    override fun getFavoriteIptvChannels(): Set<String> {
        return prefs.getStringSet(KEY_IPTV_FAVORITE_CHANNELS, emptySet()) ?: emptySet()
    }

    override fun saveFavoriteIptvChannels(channels: Set<String>) {
        prefs.edit().putStringSet(KEY_IPTV_FAVORITE_CHANNELS, channels).apply()
    }

    override fun getAppLayoutMode(): String {
        return prefs.getString(KEY_APP_LAYOUT_MODE, "COMPACT_HIGH_DENSITY") ?: "COMPACT_HIGH_DENSITY"
    }

    override fun setAppLayoutMode(mode: String) {
        prefs.edit().putString(KEY_APP_LAYOUT_MODE, mode).apply()
    }

    companion object {
        private const val PREFS_NAME = "samibox_prefs"
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_FAVORITE_APPS = "favorite_apps"
        private const val KEY_CUSTOM_ORDER = "custom_order"
        private const val KEY_CARD_STYLE = "card_style"
        private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
        private const val KEY_APP_CATEGORIES = "app_categories_map"
        private const val KEY_SHOW_APP_NAMES = "show_app_names"
        private const val KEY_IPTV_FAVORITE_CHANNELS = "iptv_favorite_channels"
        private const val KEY_APP_LAYOUT_MODE = "app_layout_mode"
    }
}
