package com.seventeen.edtinp

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.webkit.WebView
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.jetbrains.annotations.Nullable
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
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
    private val context: MainActivity,
    private val imageView: ImageView,
    private val webView: WebView,
    private val executor: ExecutorService,
    private val handler: Handler,
    private val cacheHandler: CacheHandler,
    private val dataHandler: DataHandler
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
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        var inputStream: InputStream? = null
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream = connection.inputStream
            Log.v("ImageDecoder", "Input Stream content : ${inputStream.read().toString()}")
            if (inputStream.read() == -1) {
                Log.d("dsf", "null is")
            }

            val bufferedInputStream = BufferedInputStream(inputStream)
            Log.v("ImageDecoder", bufferedInputStream.read().toString())
            val bitmap = BitmapFactory.decodeStream(bufferedInputStream, null, options)

            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
            Log.v("ImageDecoder", "Compress test is here")
            inputStream.close()
            if (bitmap == null) {
//                loadBitmap(string)
            }
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur", Toast.LENGTH_SHORT).show()
        } finally {
            //close input
            /*if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ioex: IOException) {
                    //Very bad things just happened... handle it
                }
            }*/
        }
        return null
    }

    /** Met à jour l'image à la semaine correspondant à l'objet MainActivity.displayedWeekId */
    fun updateImage(src: String = "", ignoreCache: Boolean = false, safeLoad : Boolean = false, saveId: Boolean = false): Boolean {
        var url = src
        var result = false
        // Crée le lien vers l'image
        if (url == "") {
            url = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=${dataHandler.getId()}&projectId=12&idPianoWeek=${MainActivity.displayedWeekId}&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=${dataHandler.getDimensions()[0]}&height=${dataHandler.getDimensions()[1]}&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1698251931648&displayConfId=15"
        }

        context.runOnUiThread {
            Glide.with(imageView)
                .asBitmap()
                .load(url)
//                .sizeMultiplier(1.toFloat())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Toast.makeText(context, "Chargement échoué", Toast.LENGTH_SHORT).show()
                        var identifiant = ""
                        if (url.split("?")[1].split("=")[0] == "identifier") {
                            identifiant = url.split("&")[0].split("?")[1].split("=")[1]
                        }
                        Log.v("ImageHandler", "Safe Updating, bad id : $identifiant")
//                        safeUpdate()

                        super.onLoadFailed(errorDrawable)
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Toast.makeText(context, "Chargement réussi", Toast.LENGTH_SHORT).show()
                        imageView.setImageBitmap(resource)
                        val key = "week${MainActivity.displayedWeekId}"
                        cacheHandler.setImage(key, resource, ignoreCache)

                        if (saveId) {
                            var identifiant = ""
                            if (url.split("?")[1].split("=")[0] == "identifier") {
                                identifiant = url.split("&")[0].split("?")[1].split("=")[1]
                            }
                            if (identifiant != "") {
                                dataHandler.setId(dataHandler.getClass(), identifiant)
                            }
                        }
                        result = true

//                        if (safeLoad) { safeUpdate() }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })


        }
        return result
    }
        fun safeUpdate(url: String = MainActivity.referenceURL, ignoreCache: Boolean = false) {

            // Crée le lien vers l'image
            val splitUrl = url.split("&") as MutableList
            splitUrl[MainActivity.idSemaineUrl] = "idPianoWeek=${MainActivity.displayedWeekId}"
            var identifiant = dataHandler.getId()
            if (identifiant != "") {
                splitUrl[0] = " https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=${identifiant}"
            }
//            val url = splitUrl.joinToString("&")
            val url = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=${identifiant}&projectId=12&idPianoWeek=${MainActivity.displayedWeekId}&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=${dataHandler.getDimensions()[0]}&height=${dataHandler.getDimensions()[1]}&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1698251931648&displayConfId=15"
            Log.v("ImageHandler", "Joined url : ${url}")
            if (splitUrl[0].split("?").size >1) {
                if (splitUrl[0].split("?")[1].split("=")[0] == "identifier") {
                    identifiant = splitUrl[0].split("?")[1].split("=")[1]
                }
            }
            LogcatToFile.saveLogcatToFile(context, "SafeLoad")

            context.runOnUiThread {
                Glide.with(imageView)
                    .asBitmap()
                    .load(url)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Toast.makeText(context, "Chargement alternatif échoué", Toast.LENGTH_SHORT).show()
                            Log.v("ImageHandler", "Failed safe update")
                            super.onLoadFailed(errorDrawable)
                        }

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            Toast.makeText(context, "Chargement réussi", Toast.LENGTH_SHORT).show()
                            Log.v("ImageHandler", "Safe update success")
                            imageView.setImageBitmap(resource)
                            val key = "week${MainActivity.displayedWeekId}"
                            cacheHandler.setImage(key, resource, ignoreCache)
                            if (identifiant != "") {
                                dataHandler.setId(dataHandler.getClass(), identifiant)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })


            }

            /*executor.execute {
            handler.post {
                val imgbtmp = loadBitmap(MainActivity.referenceURL)
            }
        }*/

            /*val imageDownloader = ImageDownloader(object : OnImageDownloadListener {
            override fun onImageDownloaded(bitmap: Bitmap) {
                Log.v("ImageDownloader", bitmap.byteCount.toString())
                // Use the downloaded bitmap here
            }

            override fun onImageDownloadFailed() {
                Log.v("ImageDownloader", "Download failed")
                // Handle the download failure here
            }
        })

        val urlm = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=17d4c5d96ada28b9dc2d1706e95ac1b6w3568&projectId=12&idPianoWeek=11&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=382&height=690&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1697128644608&displayConfId=15"
        imageDownloader.downloadImageFromUrl(urlm)*/
//        context.runOnUiThread {
//            webView.loadUrl(url)
//        }


            /*executor.execute {
            var imageBitmap = loadBitmap(MainActivity.referenceURL)!!
            try {
                imageBitmap = loadBitmap(url)!!
            } catch (e: IOException) {
                Log.v("za", "errorrrre")
                e.printStackTrace()
            }

                val key = "week${MainActivity.displayedWeekId}"
            handler.post {
                // Ajoute ou non l'image au cache après l'avoir chargée et affichée
                if (cacheHandler.isExpired() or ignoreCache) {
                    imageView.setImageBitmap(imageBitmap)
                    //TODO ajouter le jour de la semaine pour la péremption
                    cacheHandler.setImage(key, imageBitmap, ignoreCache)
                } else { //TODO à tester
                    // Si un cache est disponible, alors c'est l'image du cache qui est chargée
                    Log.d("CacheHandler", cacheHandler.getImage(key).toString())
                    if (cacheHandler.getImage(key) != null) {
                        imageView.setImageBitmap(cacheHandler.getImage(key))
                    } else {
                        updateImage(true)
                    }
                }
            }
        }*/
    }

    fun updateWebView() {
        val url = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=${dataHandler.getId()}&projectId=12&idPianoWeek=${MainActivity.displayedWeekId}&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=${dataHandler.getDimensions()[0]}&height=${dataHandler.getDimensions()[1]}&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1698251931648&displayConfId=15"
        context.findViewById<WebView>(R.id.foregroundWebView).loadUrl(url)
    }
}


/*class ImageDownloader(private val listener: OnImageDownloadListener) {

    fun downloadImageFromUrl(imageUrl: String) {
        DownloadImageTask(listener).execute(imageUrl)
    }

    private class DownloadImageTask(private val listener: OnImageDownloadListener) :
        AsyncTask<String, Void, Bitmap?>() {

        override fun doInBackground(vararg params: String?): Bitmap? {
            try {
                val imageUrl = params[0]
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                return BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                listener.onImageDownloaded(result)
            } else {
                listener.onImageDownloadFailed()
            }
        }
    }
}

interface OnImageDownloadListener {
    fun onImageDownloaded(bitmap: Bitmap)
    fun onImageDownloadFailed()
}*/

