package com.seventeen.edtinp

import android.R.attr.src
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.ExecutorService



/** Gestionnaire de l'affichage de l'image de l'emploi du temps
 * @param imageView ImageView qui doit afficher l'image
 */
class ImageHandler
    (
    private val context: Context,
    private val imageView: ImageView,
    private val executor: ExecutorService,
    private val handler: Handler,
    private val cacheHandler: CacheHandler
) {
    /** @return Retourne un objet URL à partir de l'adresse donnée en paramètre */
    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Récupère le bitmap à partir d'une url
     */
    private fun loadBitmap(string: String): Bitmap? {
        var url: URL = mStringToURL(string)!!
//        val string = "https://upload.wikimedia.org/wikipedia/commons/archive/5/53/20170301123009%21Google_%22G%22_Logo.svg"
        val string = "https://picsum.photos/id/237/200/300"
        val connection: HttpURLConnection?
        try {
            url = URL(string)
            var connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            Log.v("hgdf", "${input.read()}")
            return BitmapFactory.decodeStream(input)
//            connection = url.openConnection() as HttpURLConnection
//            connection.connect()
//            val inputStream: InputStream = connection.inputStream
//            val bufferedInputStream = BufferedInputStream(inputStream)
//            Log.v("is", "${inputStream.read()}")
//            val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    /** Met à jour l'image à la semaine correspondant à l'objet MainActivity.displayedWeekId */
    fun updateImage(ignoreCache: Boolean = false, forcereference: Boolean = false) {

        // Crée le lien vers l'image
        val spliturl = MainActivity.referenceURL.split("&") as MutableList
        spliturl[MainActivity.idSemaineUrl] = "idPianoWeek=${MainActivity.displayedWeekId}"
        val url = spliturl.joinToString("&")
//        executor.execute { val imageBitmap = loadBitmap(url); Log.v("bitmap", imageBitmap.toString()) }
        executor.execute {
            handler.post{
                Glide.with(context).load(url).into(imageView)
            };
        }
//        if (forcereference) {
//
//            url = MainActivity.referenceURL
//            Log.v("a", url)
//        }
//        executor.execute {
//
//            val key = "week${MainActivity.displayedWeekId}"
//
//            handler.post {
//                // Ajoute ou non l'image au cache après l'avoir chargée et affichée
//                if (cacheHandler.isExpired() or ignoreCache) {
//                    imageView.setImageBitmap(imageBitmap)
//                    //TODO ajouter le jour de la semaine pour la péremption
//                    cacheHandler.setImage(key, imageBitmap!!, ignoreCache)
//                } else { //TODO à tester
//                    // Si un cache est disponible, alors c'est l'image du cache qui est chargée
//                    Log.d("CacheHandler", cacheHandler.getImage(key).toString())
//                    if (cacheHandler.getImage(key) != null) {
//                        imageView.setImageBitmap(cacheHandler.getImage(key))
//                    } else {
//                        updateImage(true)
//                    }
//                }
//            }
//        }
    }
}

