package com.seventeen.edtinp


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), DatePicker.OnDatePass {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var backgroundWebView: WebView

    val mainUrl = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

    val jsSetReferenceDelay = 800

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ajoute la barre d'outil supérieure
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialisation des paramètres de la classe ImageHandler, qui est chargée de l'affichage de l'edt
        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())
        val imageView: ImageView = findViewById(R.id.imageView)
        // Initialisation de ImageHandler, cet objet n'est créé qu'une seule fois dans l'Activité
        val imageHandler = ImageHandler(this, imageView, myExecutor, myHandler)

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
        backgroundWebView.visibility = View.INVISIBLE


        // Initialisation du calendrier de sélection de date
        val dateButton = findViewById<ImageView>(R.id.calendar_button)
        dateButton.setOnClickListener {
            val datePicker =
                DatePicker(imageHandler)
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

        DataHandler().setup(this) // Initialise le gestionnaire de données

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
        Log.v("Date Handler", "Week number is $currentWeekNumber week id is $currentWeekId")


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
                                Log.d("Preloader", "Found cached week, reloading to $currentWeekId")
                                //TODO push nécessaire ? Est-il possible de juste recharger au jour J ?
                                backgroundWebView.evaluateJavascript(
                                    js_functions + "push($displayedWeekId, true);",
                                    null
                                )
                            }


                            //TODO délais

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

        // Charge la WebView d'arrère-plan
        backgroundWebView.loadUrl(mainUrl)

        // Initialisation des boutons de navigation
        val prevButton = findViewById<Button>(R.id.prev_week)
        val nextButton = findViewById<Button>(R.id.next_week)

        //TODO désactiver les boutons par défaut et les activer lorsque la page est affichée ?

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
    }


    /** sauvegarde la nouvelle classe */
    private fun changeClasse(classe: String) {

        DataHandler.data.classe = classe
        DataHandler().updateSave(this)
    }

    /** Initialise le menu en haut à gauche en ajoutant le nom de l'appli à côté */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val hamButton: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_baseline_menu_24)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        // Met l'icône de menu en tant que bouton de navigation
//        toolbar.navigationIcon = hamButton

        menuInflater.inflate(R.menu.menu, menu)
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        classeTextView.text = DataHandler.data.classe
        return true
    }

    /** Fonction appelée lorsque l'utilisateur effectue un clic sur un jour du calendrier
     * Met à jour l'objet selectedWeekId
     * @param weekId Id de la semaine à stocker dans l'objet selectedWeekId */
    override fun onDatePass(weekId: Int, activity: FragmentActivity?) {
        Log.v("TempSave", "Updated to $weekId")
        selectedWeekId = weekId
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /** Callback lors d'un clic dans le menu de sélection de classe en haut à droite  */
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
            //TODO boucle infinie
            if (url == "") {
                backgroundWebView.post {
                    backgroundWebView.evaluateJavascript(
                        set_reference_url,
                        null
                    )
                }; return
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
                spliturl[idSemaineUrl +1] = "idPianoDay=0%2C1%2C2%2C3%2C4"
                referenceURL = spliturl.joinToString("&")

                Log.v(
                    "ImageHandler",
                    "Reference url set to $referenceURL and week id set to $idSemaineUrl"
                )
                makeToast("Connection établie")

                // Affiche l'image de référence
                imageHandler.updateImage()
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
    }
}

/** Gestionnaire de l'affichage de l'image de l'emploi du temps
 * @param imageView ImageView qui doit afficher l'image
 */
class ImageHandler
    (
    private val context: Context,
    private val imageView: ImageView,
    private val executor: ExecutorService,
    private val handler: Handler
) {
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

    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
        }
        return null
    }

    /** Met à jour l'image à la semaine correspondant à l'objet MainActivity.displayedWeekId */
    fun updateImage() {

        // Crée le lien vers l'image
        val spliturl = MainActivity.referenceURL.split("&") as MutableList
        spliturl[MainActivity.idSemaineUrl] = "idPianoWeek=${MainActivity.displayedWeekId}"
        val url = spliturl.joinToString("&")

//        Log.v("call", "CALLED ${MainActivity.displayedWeekId}")
        executor.execute {
            val mImage = mLoad(url)
            handler.post {
                imageView.setImageBitmap(mImage)
//                if (mImage != null) {
//                    mSaveMediaToStorage(mImage)
//                }
            }
        }
    }
}