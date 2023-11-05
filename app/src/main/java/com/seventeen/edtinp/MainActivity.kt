package com.seventeen.edtinp


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.rgb
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.Executors
import kotlin.concurrent.thread


//TODO supprimer le code inutile

class MainActivity : AppCompatActivity(), DatePicker.OnDatePass {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var backgroundWebView: WebView
    lateinit var foregroundWebView: WebView
    lateinit var imageView: ImageView
    val mainUrl = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

    val jsSetReferenceDelay = 1200
    val jsSetBitmapDelay = 100

    private lateinit var dataHandler: DataHandler
    private lateinit var cacheHandler: CacheHandler
    private lateinit var imageHandler: ImageHandler
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ajoute la barre d'outil supérieure
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialisation des paramètres de la classe ImageHandler, qui est chargée de l'affichage de l'edt
        imageView = findViewById(R.id.imageView)
        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())
        dataHandler = DataHandler(this) // Initialise le gestionnaire de données
        imageHandler = ImageHandler(this, imageView, dataHandler)
        cacheHandler = CacheHandler(this, dataHandler)


        // Initialisation de la WebView d'arrière plan nécessaire pour générer le lien de référence
        backgroundWebView = findViewById(R.id.backgroundWebView)
        backgroundWebView.settings.javaScriptEnabled = true
        backgroundWebView.addJavascriptInterface(BackgroundWebViewJavaScriptInterface(this, imageHandler, dataHandler), "app")


        // Initialisation de la WebView intermédiaire nécessaire pour générer des images à partir de l'url obtenue en amont
        foregroundWebView = findViewById<WebView>(R.id.foregroundWebView)
        foregroundWebView.settings.javaScriptEnabled = true
        foregroundWebView.setBackgroundColor(Color.argb(1, 0, 0, 0))
        foregroundWebView.addJavascriptInterface(ForegroundWebViewJavaScriptInterface(this, imageHandler), "app")


        // Initialisation des boutons de navigation
        val prevButton = findViewById<Button>(R.id.prev_week)
        prevButton.isEnabled = false
        val nextButton = findViewById<Button>(R.id.next_week)
        nextButton.isEnabled = false
        val refreshButton = findViewById<ImageView>(R.id.refresh_button)
        refreshButton.isEnabled = false
        refreshButton.setColorFilter(rgb(184, 184, 184)) // Filtre gris tant que la connexion n'est pas établie




        // Initialisation du calendrier de sélection de date
        val dateButton = findViewById<ImageView>(R.id.calendar_button)
        dateButton.setOnClickListener {
            val datePicker =
                DatePicker(imageHandler, dataHandler)
            datePicker.show(supportFragmentManager, DatePicker.TAG)
        }

        // On agrandit la taille du webView pour optimiser l'affichage
        // Nécessaire pour générer une image de la bonne taille
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        Log.d(
            "WebViewHandler",
            "Gained pixels: ${width * 18 / 100}"
        ) // 18% : taille trouvée dans le html
        backgroundWebView.layoutParams.width = width + width * 18 / 100 + 25

        // Obtention de l'id de la semaine actuelle
        val calendar = Calendar.getInstance()
        var currentWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) or (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
            currentWeekNumber += 1
        }
        val currentWeekId: Int = if (currentWeekNumber < 32) {
            currentWeekNumber + 20
        } else {
            currentWeekNumber - 32
        }
        displayedWeekId = currentWeekId

        // Mise à jour des données
        dataHandler.setCurrentWeekId(currentWeekId)
        dataHandler.setCurrentDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1)
        dataHandler.updateSave()

        Log.v("Date Handler", "Week number is $currentWeekNumber week id is $currentWeekId")

        // Affiche l'image de la semaine gardée en cache
        var imageBitmap = cacheHandler.getImage("week$displayedWeekId")
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap)
        }

        // Affiche l'image de la semaine gardée en cache
        imageBitmap = cacheHandler.getImage("week${displayedWeekId-1}")
        if (imageBitmap != null) {
            val cacheDir = this.cacheDir
            val cacheFile = File(cacheDir, "week${displayedWeekId-1}")
            deleteDir(cacheFile)
            Toast.makeText(this, "Image précédente effacée", Toast.LENGTH_SHORT).show()
        }

        var javascript = """
                    javascript:(function() {
                        var body = document.querySelector('body');
                        if (body) {
                            body.style.backgroundColor = 'transparent';
                            console.log('good news ! :)');
                            setTimeout(function() {app.setBitmap();}, $jsSetBitmapDelay);
                        } else { console.log('I tried :('); };
                    })();
                """.trimIndent()
        javascript = """
                    javascript:(function() {
                        var body = document.querySelector('body');
                        if (body) {
                            body.style.backgroundColor = 'transparent';
                            console.log('good news ! :)');
                            setTimeout(function() {app.setBitmap();}, $jsSetBitmapDelay+100);
                        } else { console.log('I tried :('); setTimeout(function() {$javascript}, 200); };
                    })();
                """.trimIndent()


        // Client de la WebView intermédiaire
        foregroundWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                foregroundWebView.evaluateJavascript(javascript, null)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        // Client WebView d'arrière-plan
        backgroundWebView.webViewClient = object : WebViewClient() {
            var isRedirected = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (!isRedirected) {
                    Log.v("URL_Loader", "Loading $url")
                }
                isRedirected = false
            }

            @Deprecated("Ignore")
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url!!)
                isRedirected = true
                return true
            }

            override fun onPageFinished(view: WebView?, url: String) {
                super.onPageFinished(view, url)
                if (isRedirected) {
                    Log.v("URL_Loader", "Redirected from $url")
                }

                // Mise en page du contenu
                if (url != mainUrl) {
                    Log.v("URL_Loader", "Loading page")
                    var search = ""
                    when (dataHandler.getClass()) {
                        "1A-PINP" -> search = search1A
                        "2A-PINP" -> search = search2A
                        "HN1-PINP" -> search = searchHN1
                        "HN2-PINP" -> search = searchHN2
                        "HN3-PINP" -> search = searchHN3
                    }
                    val jsCode =
                        (preload + search + load)
                    backgroundWebView.evaluateJavascript(jsCode, null)

                    // Vérification de la semaine affichée
                    backgroundWebView.evaluateJavascript(get_selected_week) {
                        var cacheWeekNumber: String // Prend la valeur retournée par le code javascript
                        if ((it == "null") or (it.length < 5)) {
                            Log.d("Preloader", "Got null resource")
                        } else {
                            cacheWeekNumber = it.subSequence(2, 4).toString()
                            if (cacheWeekNumber[0].toString() == " ") {
                                cacheWeekNumber = cacheWeekNumber[0].toString()
                            }
                            if ((cacheWeekNumber.toInt() != currentWeekNumber) and (cacheWeekNumber.toInt() != currentWeekNumber)) {
                                Log.d("Preloader", "Found wrong week : $cacheWeekNumber, reloading to $currentWeekId")
                                //TODO push nécessaire ? Est-il possible de juste recharger au jour J ?
                                backgroundWebView.evaluateJavascript(
                                    js_functions + "push($displayedWeekId, true);",
                                    null
                                )
                            }

                            // Initialise les objets de la classe
                            backgroundWebView.evaluateJavascript(
                                "setTimeout(function() {$set_reference_url}, $jsSetReferenceDelay)",
                                null
                            )
                        }
                    }
                }
            }
        }

        prevButton.setOnClickListener {
            backgroundWebView.evaluateJavascript(check_edt_availability) {// Nécessaire pour invalider le clic si page non chargée
                if (it != "null") {
                    val result =
                        it.toInt() // Taille de la recherche du tag img dans la fenêtre, 1 si edt visible, 0 sinon

                    // Incrémentation de l'id de la semaine
                    if ((displayedWeekId > 0) and (result != 0)) { // result == 0 => edt non visible
                        displayedWeekId -= 1
                        Log.v("Date Handler", "Moving to week $displayedWeekId")
                        // Met l'image dans une ImageView
                        imageHandler.updateWebView()
                    }
                }
            }
        }
        nextButton.setOnClickListener {
            backgroundWebView.evaluateJavascript(check_edt_availability) {// Nécessaire pour invalider le clic si page non chargée
                if (it != "null") {
                    val result =
                        it.toInt() // Taille de la recherche du tag img dans la fenêtre, 1 si edt visible, 0 sinon

                    // Incrémentation de l'id de la semaine
                    if ((displayedWeekId < 51) and (result != 0)) { // Result == 0 => edt non visible
                        displayedWeekId += 1
                        Log.v("Date Handler", "Moving to week $displayedWeekId")
                        // Met à jour l'image
                        imageHandler.updateWebView()
                    }
                }
            }
        }

        // Initialisation du bouton de rafraîchissement
        refreshButton.setOnClickListener {
            thread {
                if (isOnline()) {
                    runOnUiThread {
                        switchNavigation(this@MainActivity, navigationValue = false)
                        backgroundWebView.loadUrl(mainUrl)
                    }
                } else {
                    // Affiche un dialogue et propose de rester en mode hors ligne
                    //TODO ajouter une checkbox pour ne plus afficher le dialogue
                    this.runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Pas de connexion internet")
                            .setMessage("Vous pouvez quand même consulter la semaine actuelle, fermer l'application ?")
                            .setPositiveButton("Fermer") { dialog, _ ->
                                dialog.dismiss(); finishAndRemoveTask()
                            }
                            .setNegativeButton("Rester") { dialog, _ ->
                                dialog.dismiss()
                                runOnUiThread { switchNavigation(this@MainActivity, navigationValue = false, loadFailure = true) }
                                }
                            .show()
                    }
                }
            }
        }



        thread {
            // Vérification de la connectivité à internet
            if (isOnline()) {
                // Charge la WebView d'arrère-plan
                runOnUiThread {backgroundWebView.loadUrl(mainUrl) }
            } else {
                // Affiche un dialogue et propose de rester en mode hors ligne
                //TODO ajouter une checkbox pour ne plus afficher le dialogue
                this.runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("Pas de connexion internet")
                        .setMessage("Vous pouvez quand même consulter la semaine actuelle, fermer l'application ?")
                        .setPositiveButton("Fermer") { dialog, _ ->
                            dialog.dismiss(); finishAndRemoveTask()
                        }
                        .setNegativeButton("Rester") { dialog, _ ->
                            dialog.dismiss()
                            runOnUiThread { switchNavigation(this@MainActivity, navigationValue = false, loadFailure = true) }
                        }
                        .show()
                }
            }
        }
    }




    override fun onStop() {
        super.onStop()
        if (dataHandler.getLoggingState()) {
            LogcatToFile.saveLogcatToFile(this)
        }
    }

    /**
     * Récupère le contenu de la WebView intermédiaire sous forme de Bitmap puis sauvegarde ce Bitmap dans le cache sous forme d'image
     */
    private fun captureWebViewContent() {
        val bitmap = getBitmapFromWebView()
        // Récupère le contenu de la WebView intermédiaire
        val canvas = Canvas(bitmap)
        foregroundWebView.draw(canvas)

        // Enregistre le Bitmap dans le cache
        val key = "week${MainActivity.displayedWeekId}"
        cacheHandler.setImage(key, bitmap)
    }

    /**
     * Récupère le contenu de la WebView intermédiaire sous forme de Bitmap
     * @return Le Bitmap sur lequel a été imprimé le contenu de la WebView intermédiaire
     */
    fun getBitmapFromWebView(): Bitmap {
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


    /** Sauvegarde la nouvelle classe */
    private fun changeClasse(classe: String) {
        dataHandler.setClass(classe)
    }

    /** Essaie une transaction internet
     * @return true si connexion à internet, et false sinon
     */
    private fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }


    //TODO comparer les anciennes versions pour vérifier si on peut supprimer
    /** Fonction appelée lorsque l'utilisateur effectue un clic sur un jour du calendrier
     * Met à jour l'objet selectedWeekId
     * @param weekId Id de la semaine à stocker dans l'objet selectedWeekId */
    /*override fun onDatePass(weekId: Int, activity: FragmentActivity?) {
        Log.v("TempSave", "Updated to $weekId")
        selectedWeekId = weekId
    }*/


    /** Initialise le menu en haut à gauche en ajoutant le nom de l'appli à côté */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val hamButton: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_baseline_menu_24)
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        // Met l'icône de menu en tant que bouton de navigation
//        toolbar.navigationIcon = hamButton

        menuInflater.inflate(R.menu.menu, menu)
        val classTextView = findViewById<TextView>(R.id.classe_tv)
        classTextView.text = dataHandler.getClass()
        return true
    }

    /** Active ou désactive les champs de sélection en fonction de l'objet isNavigationRestricted */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (!navigationState) {
            for (item in menu!!.children) {
                item.isEnabled = false
            }
        } else {
            for (item in menu!!.children) {
                item.isEnabled = true
                if (item.title == "Logs") {
                    item.isChecked = dataHandler.getLoggingState()
                }
            }
        }
        return true
    }

    /** Fonction appelée lorsque l'utilisateur effectue un clic sur un jour du calendrier
     * Met à jour l'objet selectedWeekId
     * @param weekId Id de la semaine à stocker dans l'objet selectedWeekId */
    override fun onDatePass(weekId: Int, activity: FragmentActivity?) {
        Log.v("TempSave", "Updated to $weekId")
        selectedWeekId = weekId
    }

    /** Callback lors d'un clic dans le menu de sélection de classe en haut à droite  */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var search = ""
        when (item.title) {
            getString(R.string.A1) -> {
                changeClasse("1A-PINP")
                Log.v("Menu Handler", dataHandler.getClass())
                search = search1A
            }

            getString(R.string.A2) -> {
                changeClasse("2A-PINP")
                Log.v("Menu Handler", dataHandler.getClass())
                search = search2A
            }

            getString(R.string.HN1) -> {
                changeClasse("HN1-PINP")
                Log.v("Menu Handler", dataHandler.getClass())
                search = searchHN1
            }

            getString(R.string.HN2) -> {
                changeClasse("HN2-PINP")
                Log.v("Menu Handler", dataHandler.getClass())
                search = searchHN2
            }

            getString(R.string.HN3) -> {
                changeClasse("HN3-PINP")
                Log.v("Menu Handler", dataHandler.getClass())
                search = searchHN3
            }
            "Effaçage général" -> { flushCache(applicationContext, true) }
            "Effaçage simple" -> { flushCache(applicationContext) }
            "Effaçer le fichier de sauvegarde" -> { dataHandler.flushData() }
            "Logs" -> { item.isChecked = !item.isChecked; dataHandler.setLoggingState(item.isChecked) }
        }

        // Met le nom de la class à jour
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        classeTextView.text = dataHandler.getClass()


        // En cas de besoin, choisir une classe recharge la page (permet de sortir d'un bug)
        //TODO nécéssaire ?
        if (item.title !in arrayOf("Effaçage général", "Effaçage simple", "Effaçer le fichier de sauvegarde", "Logs")) {
            runOnUiThread { switchNavigation(this@MainActivity, navigationValue = false) }
            backgroundWebView.evaluateJavascript(get_selected_week) {


                if ((it == "null") or (it.length < 5)) {
                    // Cas du bug
                    Log.d("Preloader", "Got null resource"); backgroundWebView.loadUrl(mainUrl)
                } else {
                    // Si la classe est changée, alors on remet à jour la page
                    Log.v("DEBUG", "reload")
                    backgroundWebView.evaluateJavascript(preload + search + load, null)
                    backgroundWebView.evaluateJavascript(
                        js_functions + "push($displayedWeekId, true)",
                        null
                    )

                    // Initialise les objets de la classe
                    backgroundWebView.evaluateJavascript(
                        "setTimeout(function() {$set_reference_url}, $jsSetReferenceDelay)",
                        null
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*
     * JavaScript Interface. Web code can access methods in here
     * (as long as they have the @JavascriptInterface annotation)
     */
    /**
     * @param imageHandler ImageHandler responsable du chargement de l'image
     */
    class BackgroundWebViewJavaScriptInterface(
        private val context: MainActivity,
        private val imageHandler: ImageHandler,
        private val dataHandler: DataHandler
    ) {
        @JavascriptInterface
        fun makeToast(message: String?, lengthLong: Boolean = true) {
            Toast.makeText(context, message, if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }

        /** Met à jour les objets de l'activité et affiche l'image avec la nouvelle référence
         * Objets mis à jour :  referenceUrl; idSemaineUrl
         * Cette méthode est appelée lorsque l'on change de classe ou lors du lancement de l'application */
        @JavascriptInterface
        fun setReferenceUrl(url: String) {
            if (url == "") {
                makeToast("Erreur lors de la récupération du lien"); return
            } else {
                referenceURL = url

                // Met à jour l'objet id de la semaine
                //TODO Remplacer toutes les créations d'url avec les placeholder
                val splitUrl = referenceURL.split("&") as MutableList
                var widthId = 0
                var heightId = 0
                var i = 0
                for (item in splitUrl) {
                    if (item.split("=").size == 2) {
                        when (item.split("=")[0]) {
                            "idPianoWeek" -> { idSemaineUrl = i }
                            "width" -> {widthId = i}
                            "height" -> {heightId = i}
                        }
                    }
                    i += 1
                }

                // Crée le lien vers l'image avec les bon jours sélectionnés et sauvegarde la taille
                val spliturl = referenceURL.split("&") as MutableList
                spliturl[idSemaineUrl + 1] = "idPianoDay=0%2C1%2C2%2C3%2C4"
                dataHandler.setDimensions(listOf(splitUrl[widthId].split("=")[1].toInt(), splitUrl[heightId].split("=")[1].toInt()))
                referenceURL = spliturl.joinToString("&")


                // Met à jour l'identifiant si nécéssaire
                var identifiant = ""
                if (url!!.split("?")[1].split("=")[0] == "identifier") {
                    identifiant = url.split("&")[0].split("?")[1].split("=")[1]
                }
                if (((identifiant != "") and (identifiant != dataHandler.getId()))) {
                    dataHandler.setId(dataHandler.getClass(), identifiant)
                }


                // Charge l'url dans la WebView intermédiaire
                context.runOnUiThread{ context.findViewById<WebView>(R.id.foregroundWebView).loadUrl(referenceURL) }
                Log.v("JavaInterface", "Reference url set to $referenceURL and week field id set to $idSemaineUrl")

                // Logs diverses
                // TODO passer en commentaires plus tard
                val files = context.cacheDir.listFiles()
                if (files != null) {
                    Log.d("CacheHandler", "Size: " + files.size)
                }
                if (files != null) {
                    for (i in files.indices) {
                        Log.d("CacheHandler", "FileName:" + files[i].name)
                    }
                }

                // Sauvegarde l'image de la semaine dans le cache
                context.captureWebViewContent()
                // Efface le fichier de la semaine anteprécédente
                deleteDir(context.cacheDir, weekId = dataHandler.getCurrentWeekId() - 2)
                makeToast("Connexion établie", false)
            }
        }

        /** Fonction appelée en cas d'échec lors de la récupération du lien de référence de l'image */
        @JavascriptInterface
        fun onLoadingFail() {
            makeToast("Chargement arrière échoué")
            Log.v("JavaInterface", "Back load failed")
            context.runOnUiThread { switchNavigation(context, navigationValue = false, loadFailure = true) }
        }
    }

    /**
     * Interface utilisée pour la WebView intermédiaire
     * @param imageHandler ImageHandler responsable du chargement de l'image
     */
    class ForegroundWebViewJavaScriptInterface(
        private val context: MainActivity,
        private val imageHandler: ImageHandler
    ) {
        @JavascriptInterface
        fun makeToast(message: String?, lengthLong: Boolean = true) {
            Toast.makeText(context, message, if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }

        /**
         * Applique le contenu de la WebView intermédiaire sur un Bitmap
         * @return Le Bitmap sur lequel a été imprimé le contenu de la WebView
         */
        @JavascriptInterface
        fun setBitmap() {
            val foregroundWebView = context.findViewById<WebView>(R.id.foregroundWebView)
            // Récupère la dimension de la WebView intermédiaire
            val width = foregroundWebView.width
            val height = foregroundWebView.height

            // Crée le Bitmap vide correspondant
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // Imprime le contenu de la WebView intermédiaire sur le Bitmap
            val canvas = Canvas(bitmap)
            foregroundWebView.draw(canvas)

            // Rétablit la navigation avant de mettre à jour l'ImageView principale
            context.runOnUiThread { switchNavigation(context, navigationValue = true) }
            context.findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)
        }
    }

    companion object {
        var referenceURL: String = ""
        var idSemaineUrl: Int = 0
        var selectedWeekId: Int = 0
        var displayedWeekId: Int = 0
        var navigationState: Boolean = false
    }
}

fun isDarkModeEnabled(context: Context): Boolean {
    val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
}


//TODO commenter
fun switchNavigation(context: MainActivity, navigationValue: Boolean? = null, loadFailure: Boolean = false) {
    if (navigationValue != null) {
        MainActivity.navigationState = navigationValue
    } else {
        MainActivity.navigationState = !MainActivity.navigationState
    }
        MainActivity.navigationState = MainActivity.navigationState
        context.findViewById<Button>(R.id.prev_week).isEnabled = MainActivity.navigationState
        context.findViewById<Button>(R.id.next_week).isEnabled = MainActivity.navigationState
        context.findViewById<ImageView>(R.id.refresh_button).isEnabled = MainActivity.navigationState
        if (MainActivity.navigationState) {
            context.findViewById<ImageView>(R.id.refresh_button).setColorFilter(rgb(255, 255, 255))
            context.findViewById<TextView>(R.id.loading_tv).visibility = View.GONE
        } else {
            context.findViewById<ImageView>(R.id.refresh_button).setColorFilter(rgb(184, 184, 184))
            context.findViewById<TextView>(R.id.loading_tv).visibility = View.VISIBLE
        }
        if (loadFailure) {
            context.findViewById<ImageView>(R.id.refresh_button).isEnabled = true
            context.findViewById<ImageView>(R.id.refresh_button).setColorFilter(rgb(255, 255, 255))
            context.findViewById<TextView>(R.id.loading_tv).visibility = View.GONE
        }
}


/**
 * Vide les images en cache
 */
fun flushCache(context: Context, full: Boolean = false) {
    val cacheDir = context.cacheDir
    if (cacheDir != null && cacheDir.isDirectory()) {
        deleteDir(cacheDir, full)
        Log.d("CacheHandler", "Erased file ${cacheDir.name}")
    } else { Log.d("CacheHandler", "Something went wrong") }
}

/**
 * Efface les fichier dans un répertoire donné en paramètre
 */
fun deleteDir(dir: File?, full: Boolean = false, weekId: Int? = null): Boolean {
    if (dir != null && dir.isDirectory) {
        val children = dir.list()
        for (i in children.indices) {
            if ("week" in children[i]) {
                if ((weekId == null) or ((weekId != null) and (children[i] == "week${weekId}"))) {
                    val success = deleteDir(File(dir, children[i]))
                    Log.d("CacheEraser", "Erased ${children[i]}")
                    if (!success) {
                        return false
                    }
                }
            } else {
                if (full) {
                    val success = deleteDir(File(dir, children[i]))
                    Log.d("CacheEraser", "Erased ${children[i]}")
                    if (!success) {
                        return false
                    }
                }
            }
        }
    }

    // The directory is now empty so delete it
    return dir!!.delete()
}