package com.seventeen.edtinp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class CacheHandler(private val context: Context, private val dataHandler: DataHandler) {
    lateinit var cacheFile: File
    init {
        cacheFile = File(context.cacheDir, "ImageCache")
//        if (cacheFile.listFiles() != null) { Log.d("CacheHandler", "Found cache"); Log.d("CacheHandler", cacheFile.listFiles().toString()) }
//        else { Log.d("CacheHandler", "No cache found"); createCache()}
    }
    fun setImage(key: String, value: Bitmap, ignoreCache: Boolean = false) {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, key)

        /*var previousWeek: Int
        var thirdWeek: Int

        if (dataHandler.getCurrentWeekId() > 0) {
            previousWeek = dataHandler.getCurrentWeekId()-1
        } else { previousWeek = 0}

        if (dataHandler.getCurrentWeekId()+2 < 51) {
            thirdWeek = dataHandler.getCurrentWeekId()+2
        } else { thirdWeek = 51 }*/

//        if (MainActivity.displayedWeekId in (previousWeek..thirdWeek)) {
        if (MainActivity.displayedWeekId == dataHandler.getCurrentWeekId()) {
            if ((getImage(key) == null) or (ignoreCache)) {
                try {
                    val outputStream = FileOutputStream(file)
                    value.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    Log.d("CacheHandler", "Saved to cache")
                    if (ignoreCache) {
                        Toast.makeText(context, "Mise à jour réussie", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    fun getImage(key: String): Bitmap? {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, key)

        if (file.exists()) {
            return BitmapFactory.decodeFile(file.absolutePath)
        } else {
            return null
        }
    }

    fun isExpired(): Boolean {
        return true
    }

    private fun createCache() {
        File.createTempFile("ImageCache", null, context.cacheDir)
    }
}