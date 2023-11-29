/**
 * Copyright (C)  - All Rights Reserved
 *
 * Copyright details are in the LICENSE.md file located in the root of this Android project.
 * Everything written in the LICENSE.md file applies on this file.
 *
 * Any unauthorized copying, editing, or publishing, even partial, of this file is strictly forbidden.
 *
 * Owner of this file, its content, and the copyright related : Paul Musial, paul.musial.dev@gmail.com
 */
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
        //TODO Nettoyer test plus tard
        if ((MainActivity.displayedWeekId == dataHandler.getCurrentWeekId()) or true) {
            //TODO Nettoyer test ici aussi
            if (true or ((getImage(key) == null) or (ignoreCache))) {
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
                if ("week" in children[i]) { // Donc le fichier est une image de cache
                    if (("week${weekId}" !in children[i]) and ("week${weekId+1}" !in children[i])) { // On ne veut pas supprimer le cache actuel ou suivant
                        val success = deleteDir(File(cacheDir, children[i])) // En revanche, on veut supprimer tous les auteres
                        Log.d("CacheEraser", "Erased ${children[i]}")
                        if (!success) {
                            return false
                        }
                    }
                }
            }
        }

        //TODO supprimer ?
        return cacheDir!!.delete() // Supprime le fichier de cache
    }

    fun listCache() {
        val directory = context.cacheDir
        val files = directory.listFiles()
        if (files != null) {
            Log.d("CacheHandler", "Size: " + files.size)
        }
        if (files != null) {
            for (i in files.indices) {
                Log.d("CacheHandler", "FileName:" + files[i].name)
            }
        }
    }
}