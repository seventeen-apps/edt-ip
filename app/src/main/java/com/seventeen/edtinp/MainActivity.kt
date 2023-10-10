package com.seventeen.edtinp


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
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
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.Executors
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), DatePicker.OnDatePass {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var backgroundWebView: WebView

    val mainUrl = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

    val jsSetReferenceDelay = 1000

    private lateinit var dataHandler: DataHandler

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ajoute la barre d'outil supérieure
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialisation des paramètres de la classe ImageHandler, qui est chargée de l'affichage de l'edt
        val imageView: ImageView = findViewById(R.id.imageView)
        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())
        dataHandler = DataHandler(this) // Initialise le gestionnaire de données
        val cacheHandler = CacheHandler(this, dataHandler)
        // Initialisation de ImageHandler, cet objet n'est créé qu'une seule fois dans l'Activité
        val imageHandler = ImageHandler(this, imageView, myExecutor, myHandler, cacheHandler)

        // Initialisation de la WebView d'arrière plan nécessaire pour générer des images à la taille de l'écran
        backgroundWebView = findViewById(R.id.backgroundWebView)
        backgroundWebView.settings.javaScriptEnabled = true
        backgroundWebView.addJavascriptInterface(
            WebViewJavaScriptInterface(
                this,
                backgroundWebView,
                imageHandler
            ), "app"
        )
        backgroundWebView.visibility = View.VISIBLE


        // Initialisation du calendrier de sélection de date
        val dateButton = findViewById<ImageView>(R.id.calendar_button)
        dateButton.setOnClickListener {
            val datePicker =
                DatePicker(imageHandler)
            datePicker.show(supportFragmentManager, DatePicker.TAG)
        }

        // Initialisation du bouton de rafraîchissement
        val refreshButton = findViewById<ImageView>(R.id.refresh_button)
        refreshButton.setOnClickListener {
            imageHandler.updateImage(true)
        }
        refreshButton.isEnabled = false
        refreshButton.setColorFilter(
            rgb(
                184,
                184,
                184
            )
        ) // Filtre gris tant que la connexion n'est pas établie


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
        DataHandler.data.currentWeekId = currentWeekId
        DataHandler.data.currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        dataHandler.updateSave()

        Log.v("Date Handler", "Week number is $currentWeekNumber week id is $currentWeekId")

        // Affiche l'image de la semaine gardée en cache
        val imageBitmap = cacheHandler.getImage("week${displayedWeekId}")
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap)
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
                    when (DataHandler.data.classe) {
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
                            if (cacheWeekNumber.toInt() != currentWeekNumber) {
                                Log.d("Preloader", "Found wrong week, reloading to $currentWeekId")
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


        // Initialisation des boutons de navigation
        val prevButton = findViewById<Button>(R.id.prev_week)
        prevButton.isEnabled = false
        val nextButton = findViewById<Button>(R.id.next_week)
        nextButton.isEnabled = false

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
                        imageHandler.updateImage()
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
                        imageHandler.updateImage()
                    }
                }
            }
        }

        // Vérification de la connectivité à internet
        thread {
            if (isOnline()) {
                this.runOnUiThread {
                    // Charge la WebView d'arrère-plan
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
                            prevButton.isEnabled = false
                            nextButton.isEnabled = false
                            isNavigationRestricted = true
                        }
                        .show()
                }
            }
        }
    }


    /** Sauvegarde la nouvelle classe */
    private fun changeClasse(classe: String) {
        DataHandler.data.classe = classe
        dataHandler.updateSave()
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

    /** Initialise le menu en haut à gauche en ajoutant le nom de l'appli à côté */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val hamButton: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_baseline_menu_24)
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        // Met l'icône de menu en tant que bouton de navigation
//        toolbar.navigationIcon = hamButton

        menuInflater.inflate(R.menu.menu, menu)
        val classTextView = findViewById<TextView>(R.id.classe_tv)
        classTextView.text = DataHandler.data.classe
        return true
    }

    /** Active ou désactive les champs de sélection en fonction de l'objet isNavigationRestricted */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (isNavigationRestricted) {
            for (item in menu!!.children) {
                item.isEnabled = false
            }
        } else {
            for (item in menu!!.children) {
                item.isEnabled = true
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
                Log.v("Menu Handler", DataHandler.data.classe)
                search = search1A
            }

            getString(R.string.A2) -> {
                changeClasse("2A-PINP")
                Log.v("Menu Handler", DataHandler.data.classe)
                search = search2A
            }

            getString(R.string.HN1) -> {
                changeClasse("HN1-PINP")
                Log.v("Menu Handler", DataHandler.data.classe)
                search = searchHN1
            }

            getString(R.string.HN2) -> {
                changeClasse("HN2-PINP")
                Log.v("Menu Handler", DataHandler.data.classe)
                search = searchHN2
            }

            getString(R.string.HN3) -> {
                changeClasse("HN3-PINP")
                Log.v("Menu Handler", DataHandler.data.classe)
                search = searchHN3
            }
        }

        // Met le nom de la class à jour
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        classeTextView.text = DataHandler.data.classe

        // En cas de besoin, choisir une classe recharge la page (permet de sortir d'un bug)
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
        return super.onOptionsItemSelected(item)
    }

    /*
     * JavaScript Interface. Web code can access methods in here
     * (as long as they have the @JavascriptInterface annotation)
     */
    /**
     * @param backgroundWebView WebView de référence (en arrière-plan)
     * @param imageHandler ImageHandler responsable du chargement de l'image
     */
    class WebViewJavaScriptInterface    /*
        * Need a reference to the context in order to sent a post message
        */(
        private val context: Context,
        private val backgroundWebView: WebView,
        private val imageHandler: ImageHandler
    ) {

        @JavascriptInterface
                /*
                * This method can be called from Android. @JavascriptInterface
                * required after SDK version 17.
                */
        fun makeToast(message: String?, lengthLong: Boolean = true) {
            Toast.makeText(
                context,
                message,
                if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
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
                val splitUrl = referenceURL.split("&") as MutableList
                var i = 0
                for (item in splitUrl) {
                    if (item.split("=").size == 2) {
                        if (item.split("=")[0] == "idPianoWeek") {
                            idSemaineUrl = i
                        }
                    }
                    i += 1
                }

                // Crée le lien vers l'image
                val spliturl = referenceURL.split("&") as MutableList
                spliturl[idSemaineUrl + 1] = "idPianoDay=0%2C1%2C2%2C3%2C4"
                referenceURL = spliturl.joinToString("&")

                Log.v(
                    "ImageHandler",
                    "Reference url set to $referenceURL and week id set to $idSemaineUrl"
                )
                makeToast("Connexion établie", false)
                Log.v("ConnectionHandler", "Connexion établie")


                // Active tous les widgets de navigation
                isNavigationRestricted = false
                val prevButton = (context as Activity).findViewById<Button>(R.id.prev_week)
                val nextButton = context.findViewById<Button>(R.id.next_week)
                val loadingTv = context.findViewById<TextView>(R.id.loading_tv)
                val refreshButton = context.findViewById<ImageView>(R.id.refresh_button)
                context.runOnUiThread {
                    prevButton.isEnabled = true
                    nextButton.isEnabled = true
                    refreshButton.isEnabled = true
                    refreshButton.setColorFilter(rgb(255, 255, 255))
                    loadingTv.visibility = View.GONE
                }

                // Affiche l'image de référence mise à jour
                imageHandler.updateImage(forcereference = true)
            }
        }

        /** Fonction appelée en cas d'échec lors de la récupération du lien de référence de l'image */
        @JavascriptInterface
        fun onLoadingFail() {
            makeToast("Chargement échoué")
        }
    }

    companion object {
        var referenceURL: String = ""
        var idSemaineUrl: Int = 0
        var selectedWeekId: Int = 0
        var displayedWeekId: Int = 0
        var isNavigationRestricted: Boolean = true
    }
}