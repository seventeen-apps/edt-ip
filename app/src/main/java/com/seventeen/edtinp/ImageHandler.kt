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
    private val dataHandler: DataHandler
) {
    /** Met à jour l'image à la semaine correspondant à l'objet MainActivity.displayedWeekId */
    fun updateWebView() {
        val url = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=${dataHandler.getId()}&projectId=12&idPianoWeek=${MainActivity.displayedWeekId}&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=${dataHandler.getDimensions()[0]}&height=${dataHandler.getDimensions()[1]}&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1698251931648&displayConfId=15"
        context.runOnUiThread { switchNavigation(context, navigationValue = false) }
        context.findViewById<WebView>(R.id.foregroundWebView).loadUrl(url)
    }
}
