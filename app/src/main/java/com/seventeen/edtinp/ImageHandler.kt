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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebView
import android.widget.ImageView


/** Gestionnaire de l'affichage de l'image de l'emploi du temps
 */
class ImageHandler
    (
    private val context: MainActivity,
    private val dataHandler: DataHandler,
    private val cacheHandler: CacheHandler
) {
    /** Met à jour l'image à la semaine correspondant à l'objet MainActivity.displayedWeekId */
    fun updateForegroundWebView() {
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
        val key = "week${MainActivity.displayedWeekId}${dataHandler.getTreeId()}"
        cacheHandler.setImage(key, bitmap)
    }

    /**
     * Récupère le contenu de la WebView intermédiaire sous forme de Bitmap
     * @return Le Bitmap sur lequel a été imprimé le contenu de la WebView intermédiaire
     */
    fun getBitmapFromWebView(): Bitmap {
        val foregroundWebView = context.findViewById<WebView>(R.id.foregroundWebView)
        // Récupère la dimension de la WebView intermédiaire
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