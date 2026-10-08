package com.launcher.samiboxtv.util

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

object M3uFileScanner {
    fun findM3uFiles(context: Context): List<File> {
        val directories = mutableListOf<File>()

        // Memoria interna
        directories.add(Environment.getExternalStorageDirectory())
        directories.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))

        // Discos USB montados
        ContextCompat.getExternalFilesDirs(context, null).filterNotNull().drop(1).forEach { file ->
            val root = File(file.path.substringBefore("/Android"))
            if (root.exists()) directories.add(root)
        }

        val results = mutableListOf<File>()
        directories.distinctBy { it.absolutePath }.forEach { dir ->
            dir.listFiles()?.filter {
                !it.isDirectory && (it.name.endsWith(".m3u", ignoreCase = true) || it.name.endsWith(".m3u8", ignoreCase = true))
            }?.let { results.addAll(it) }
        }
        return results
    }
}