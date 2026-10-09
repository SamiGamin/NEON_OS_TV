package com.launcher.samiboxtv.util.iptv

import android.content.Context
import com.launcher.samiboxtv.domain.model.IptvPlaylist
import com.launcher.samiboxtv.domain.model.VirtualApps
import org.json.JSONArray
import org.json.JSONObject

object IptvPlaylistManager {
    private const val PREFS_NAME = "iptv_playlists_prefs"
    private const val KEY_PLAYLISTS = "saved_playlists_json"
    private const val KEY_ACTIVE_URL = "active_playlist_url"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getPrefs(context: Context? = null): android.content.SharedPreferences? {
        val ctx = context ?: appContext ?: return null
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val defaultSamplePlaylist = IptvPlaylist(
        id = "sample_default",
        name = "LISTA DE EJEMPLO (DEMO)",
        url = VirtualApps.DEFAULT_IPTV_URL,
        isDefault = true
    )

    fun getSavedPlaylists(context: Context? = null): List<IptvPlaylist> {
        val prefs = getPrefs(context) ?: return listOf(defaultSamplePlaylist)
        val jsonStr = prefs.getString(KEY_PLAYLISTS, null)
        val list = mutableListOf<IptvPlaylist>()

        list.add(defaultSamplePlaylist)

        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        IptvPlaylist(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            url = obj.getString("url"),
                            isDefault = false
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }

    fun addPlaylist(name: String, url: String, context: Context? = null): IptvPlaylist {
        val trimmedUrl = url.trim()
        val trimmedName = name.trim().ifBlank { "Lista Móvil" }
        val newPlaylist = IptvPlaylist(
            id = java.util.UUID.randomUUID().toString(),
            name = trimmedName,
            url = trimmedUrl,
            isDefault = false
        )

        val current = getSavedPlaylists(context).filter { !it.isDefault }.toMutableList()
        current.removeAll { it.url.equals(trimmedUrl, ignoreCase = true) }
        current.add(0, newPlaylist)

        saveCustomPlaylists(current, context)
        setActivePlaylistUrl(trimmedUrl, context)
        return newPlaylist
    }

    fun deletePlaylist(id: String, context: Context? = null) {
        val current = getSavedPlaylists(context).filter { !it.isDefault }.toMutableList()
        val deleted = current.find { it.id == id }
        current.removeAll { it.id == id }
        saveCustomPlaylists(current, context)

        val activeUrl = getActivePlaylistUrl(context)
        if (deleted != null && deleted.url == activeUrl) {
            setActivePlaylistUrl(VirtualApps.DEFAULT_IPTV_URL, context)
        }
    }

    fun getActivePlaylistUrl(context: Context? = null): String {
        val prefs = getPrefs(context) ?: return VirtualApps.DEFAULT_IPTV_URL
        return prefs.getString(KEY_ACTIVE_URL, VirtualApps.DEFAULT_IPTV_URL) ?: VirtualApps.DEFAULT_IPTV_URL
    }

    fun setActivePlaylistUrl(url: String, context: Context? = null) {
        val prefs = getPrefs(context) ?: return
        prefs.edit().putString(KEY_ACTIVE_URL, url).apply()
    }

    private fun saveCustomPlaylists(playlists: List<IptvPlaylist>, context: Context? = null) {
        val prefs = getPrefs(context) ?: return
        val array = JSONArray()
        for (p in playlists) {
            if (!p.isDefault) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("url", p.url)
                array.put(obj)
            }
        }
        prefs.edit().putString(KEY_PLAYLISTS, array.toString()).apply()
    }
}