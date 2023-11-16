package com.seventeen.edtinp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebView
import android.widget.ImageView


/** Gestionnaire de l'affichage de l'image de l'emploi du temps
 * @param imageView ImageView qui doit afficher l'image
 */
class ImageHandler
    (
    private val context: MainActivity,
    private val imageView: ImageView,
    private val dataHandler: DataHandler,
    private val cacheHandler: CacheHandler
) {
    /** Met à jour l'image à la semaine correspondant à l'objet MainActivity.displayedWeekId */
    fun updateWebView() {
        val url = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=${dataHandler.getId()}&projectId=12&idPianoWeek=${MainActivity.displayedWeekId}&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=${dataHandler.getTreeId()}&width=${dataHandler.getDimensions()[0]}&height=${dataHandler.getDimensions()[1]}&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1698251931648&displayConfId=15"
        context.runOnUiThread { switchNavigation(context, navigationValue = false) }
        context.findViewById<WebView>(R.id.foregroundWebView).loadUrl(url)
    }

    /**
     * Récupère le contenu de la WebView intermédiaire sous forme de Bitmap puis sauvegarde ce Bitmap dans le cache sous forme d'image
     */
    fun captureWebViewContent() {
        val foregroundWebView = context.findViewById<WebView>(R.id.foregroundWebView)
        // Récupère le Bitmap de la WebView intermédiaire
        val bitmap = getBitmapFromWebView()

        // Enregistre le Bitmap dans le cache
        val key = "week${MainActivity.displayedWeekId}${dataHandler.getClass()}"
        cacheHandler.setImage(key, bitmap)
    }

    /**
     * Récupère le contenu de la WebView intermédiaire sous forme de Bitmap
     * @return Le Bitmap sur lequel a été imprimé le contenu de la WebView intermédiaire
     */
    fun getBitmapFromWebView(): Bitmap {
        val foregroundWebView = context.findViewById<WebView>(R.id.foregroundWebView)
        // Récupère la dimension de la WebView intermédiaire
        //TODO essayer avec dataHandler.getDimensions()
        val width = foregroundWebView.width
        val height = foregroundWebView.height

        // Crée le Bitmap vide correspondant
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Imprime le contenu de la WebView intermédiaire sur le Bitmap
        val canvas = Canvas(bitmap)
        foregroundWebView.draw(canvas)

        return bitmap
    }
}
