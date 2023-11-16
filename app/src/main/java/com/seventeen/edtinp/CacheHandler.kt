package com.seventeen.edtinp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

/**
 * Gestionnaire de cache
 * @param dataHandler DataHandler unique
 */
class CacheHandler(private val context: Context, private val dataHandler: DataHandler) {

    /**
     * Ajoute un bitmap au cache
     * @param key Nom du bitmap stocké
     * @param value Contenu du bitmap
     * @param ignoreCache Booléen obligeant à invalider le cache
     */
    fun setImage(key: String, value: Bitmap, ignoreCache: Boolean = false) {
        val cacheDir = context.cacheDir
        var file = File(cacheDir, key)

        if (MainActivity.displayedWeekId == dataHandler.getCurrentWeekId()) {
            if ((getImage(key) == null) or (ignoreCache)) {
                try {
                    val outputStream = FileOutputStream(file)
                    value.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()

                    Log.d("CacheHandler", "Saved to cache file with following key : $key")
                    if (ignoreCache) {
                        Toast.makeText(context, "Mise à jour réussie", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Récupère un bitmap en cache
     * @param key Nom du bitmap à récupérer
     */
    fun getImage(key: String): Bitmap? {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, key)


        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    /** Vérifie si le bitmap en cache n'est pas trop vieux */
    fun isExpired(): Boolean {
        return true
    }

    /** Crée un fichier cache */
    private fun createCache() {
        File.createTempFile("ImageCache", null, context.cacheDir)
    }

    /**
     * Efface les images en cache obsolètes
     */
    fun clearOutdatedCache(): Boolean {
        val cacheDir = context.cacheDir
        val weekId = dataHandler.getCurrentWeekId()
        if (cacheDir != null && cacheDir.isDirectory) {
            val children = cacheDir.list()
            for (i in children.indices) {
                if ("week" in children[i]) {
                    if ("week${weekId}" !in children[i]) {
                        val success = deleteDir(File(cacheDir, children[i]))
                        Log.d("CacheEraser", "Erased ${children[i]}")
                        if (!success) {
                            return false
                        }
                    }
                }
            }
        }

        // The directory is now empty so delete it
        return cacheDir!!.delete()
    }
}

