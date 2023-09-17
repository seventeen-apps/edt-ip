package com.seventeen.edtinp


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var backgroundWebView: WebView
    lateinit var imageWebView: WebView

    val mainUrl = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

    var imgsrc = "https://via.placeholder.com/120x120&text=image1"
//        "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=dbb76b76e990cedcbb0ff47422fb9f0aw14553&projectId=12&idPianoWeek=4&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=382&height=690&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1694188961792&displayConfId=15"

    val scope = CoroutineScope(Dispatchers.IO)

    var displayedWeekId = 0
    /*suspend fun getImgRes(webView: WebView) = coroutineScope {
        Log.d("Coroutine", "Coroutine hey")
        launch {
            delay(1000)
            Log.d("Coroutine", "Coroutine Hello")
            webView.evaluateJavascript("setTimeout(function() {${getImageResource + "getImgRes()"}}, 1000)") {
//            val url = it.toString().subSequence(1, it.length-1).toString()
            Log.d("net", it)
//            imageWebView.loadUrl(url)
            }
        }
    }*/



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Ajoute la barre d'outil supérieure
        setSupportActionBar(findViewById(R.id.toolbar))


        imageWebView = findViewById(R.id.imageWebView)
        imageWebView.settings.javaScriptEnabled = true
        imageWebView.visibility = View.INVISIBLE
        imageWebView.setBackgroundColor(Color.TRANSPARENT)

        backgroundWebView = findViewById(R.id.backgroundWebView)
        backgroundWebView.settings.javaScriptEnabled = true
        backgroundWebView.addJavascriptInterface(WebViewJavaScriptInterface(this, backgroundWebView, imageWebView), "app");
        backgroundWebView.visibility = View.INVISIBLE //TODO: mettre invisible



        // On agrandit la taille du webView pour optimiser l'affichage
        // Nécessaire pour générer une image de la bonne taille
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        Log.d("WebViewHandler", "Gained pixels: ${width * 18 / 100}") // 18% : taille trouvée dans le html
        backgroundWebView.layoutParams.width = width + width * 18 / 100 + 25

        DataHandler().setup(this) // Initialise la gestion de données

        // Obtention de l'id de la semaine actuelle
        val calendar = Calendar.getInstance()
        var currentWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) or (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
            currentWeekNumber += 1
        }
        val currentWeekId: Int
        if (currentWeekNumber < 32) {
            currentWeekId = currentWeekNumber + 20
        } else {
            currentWeekId = currentWeekNumber - 32
        }
        displayedWeekId = currentWeekId
        Log.v("Date Handler", "Week number is $currentWeekNumber week id is $currentWeekId")


        // Client de la WebView principale
        imageWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                /*imageWebView.evaluateJavascript(get_html) {
                    Log.d("ImageHandler", it)
                }*/
//                imageWebView.evaluateJavascript("setTimeout(function() {document.body.setAttribute('style', 'background-color:(255, 255, 255)')}, 100);", null)
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
                    when (DataHandler.data.classe) {
                        "1A-PINP" -> search = search1A
                        "2A-PINP" -> search = search2A
                        "HN1-PINP" -> search = searchHN1
                        "HN2-PINP" -> search = searchHN2
                        "HN3-PINP" -> search = searchHN3
                    }
                    val jsCode =
                        ("setTimeout(function() {${cleanup + preload + search + load + setup_saturday + setup_sunday}}, 0)")
                    backgroundWebView.evaluateJavascript(jsCode, null)

                    // Vérification de la semaine affichée
                    backgroundWebView.evaluateJavascript(get_selected_week) {
                        var cacheWeekNumber: String
                        if ((it == "null") or (it.length < 5)) {
                            Log.d("Preloader", "Got null resource")
                        } else {
                            cacheWeekNumber = it.subSequence(2, 4).toString()
                            if (cacheWeekNumber[0].toString() == " ") {
                                cacheWeekNumber = cacheWeekNumber[0].toString()
                            }
                            if (cacheWeekNumber.toInt() != currentWeekNumber) {
                                Log.d("Preloader", "Found cached week, reloading to ${currentWeekId}")
                                displayedWeekId = currentWeekId
                                backgroundWebView.evaluateJavascript(js_functions + "push($displayedWeekId, true);", null)
                            }
                            imageWebView.visibility = View.VISIBLE

                            //TODO délais

                            // Lance l'affichage de la WebView principale
                            backgroundWebView.evaluateJavascript("setTimeout(function() {$set_image_resource}, 1000)", null)
                            // Initialise les objets de la classe
                            backgroundWebView.evaluateJavascript("setTimeout(function() {$set_reference_url}, 1000)", null)

                            // Affiche l'image de chargement
                            //TODO afficher un Dialog de chargement à la place
//                            imageWebView.loadUrl(imgsrc)
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
                    val result = it.toInt() // Taille de la recherche du tag img dans la fenêtre, 1 si edt visible, 0 sinon

                    // Incrémentation de l'id de la semaine
                    if ((displayedWeekId > 0) and (result != 0)) { // Result == 0 => edt non visible
                        displayedWeekId -= 1
                        Log.v("Date Handler", "Moving to week $displayedWeekId")

                        // Charge l'image dans la WebView principale
                        val spliturl = referenceURL.split("&") as MutableList
                        spliturl[idSemaineUrl] = "idPianoWeek=$displayedWeekId"
                        loadImage(imageWebView, spliturl.joinToString("&"))

                    }
                }
            }
        }
        nextButton.setOnClickListener {
            backgroundWebView.evaluateJavascript(check_edt_availability) {// Nécessaire pour invalider le clic si page non chargée
                if (it != "null") {
                    val result = it.toInt() // Taille de la recherche du tag img dans la fenêtre, 1 si edt visible, 0 sinon

                    // Incrémentation de l'id de la semaine
                    if ((displayedWeekId < 51) and (result != 0)) { // Result == 0 => edt non visible
                        displayedWeekId += 1
                        Log.v("Date Handler", "Moving to week $displayedWeekId")

                        // Charge l'image dans la WebView principale
                        val spliturl = referenceURL.split("&") as MutableList
                        spliturl[idSemaineUrl] = "idPianoWeek=$displayedWeekId"
//                        Log.d("Identifier", spliturl.joinToString("&"))
                        loadImage(imageWebView, spliturl.joinToString("&"))

                    }
                }
            }
        }
    }




    /** sauvegarde la nouvelle classe */
    fun changeClasse(classe: String) {

        DataHandler.data.classe = classe
        DataHandler().updateSave(this)
    }

    /** Initialise le menu en haut à gauche en ajoutant le nom de l'appli à côté */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu, menu)
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        classeTextView.text = DataHandler.data.classe
        return true
    }
    //

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
                backgroundWebView.evaluateJavascript(preload + search + load + setup_saturday + setup_sunday, null)
                backgroundWebView.evaluateJavascript(js_functions + "push($displayedWeekId, true)", null)
                // Lance l'affichage de la WebView principale
                backgroundWebView.evaluateJavascript("setTimeout(function() {$set_image_resource}, 1000)", null)
                // Initialise les objets de la classe
                backgroundWebView.evaluateJavascript("setTimeout(function() {$set_reference_url}, 1000)", null)
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
     * @param imageWebView WebView responsable de l'affichage de l'image
     */
    class WebViewJavaScriptInterface    /*
        * Need a reference to the context in order to sent a post message
        */(private val context: Context, private val backgroundWebView: WebView, private val imageWebView: WebView) {

        private var lastUrl = ""

        @JavascriptInterface
        /*
        * This method can be called from Android. @JavascriptInterface
        * required after SDK version 17.
        */
        fun makeToast(message: String?, lengthLong: Boolean) {
            Toast.makeText(
                context,
                message,
                if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }

        /** Charge l'image dans la WebView principale*/
        @JavascriptInterface
        fun setImg(src: String) {
            /*Log.d("resourcecheck", "last    " + lastUrl)
            Log.d("resourcecheck", "current " + src)*/

//            if (lastUrl == src) { backgroundWebView.post { Log.d("resourcecheck", "got same url, retrying"); backgroundWebView.evaluateJavascript(set_image_resource, null) }; return }
            if (lastUrl == "") { lastUrl = src }

            // TODO vérifier ce qu'il se passe quand on change de classe, il ne faut pas rappeler cette méthode !
            Log.v("URL_Loader", "Got src "+src)
            imageWebView.post {
                loadImage(imageWebView, src)
            }
        }


        /** Met à jour les objets de l'activité */
        @JavascriptInterface
        fun setReferenceUrl(url: String) {
            if (url == "") { backgroundWebView.post { backgroundWebView.evaluateJavascript(set_reference_url, null) }; return }
            else {
                referenceURL = url

                // Met à jour l'id de la semaine
                val splitUrl = referenceURL.split("&") as MutableList
                var i = 0
                for (item in splitUrl) {
                    if (item.split("=").size == 2) {
                        if (item.split("=")[0] == "idPianoWeek") {
                            idSemaineUrl = i
                        }
                    }
                    i+=1
                }
                Log.v("ImageHandler", "Reference url set to $referenceURL and week id set to $idSemaineUrl")
            }
        }
    }

    companion object {
        var referenceURL : String = ""
        var idSemaineUrl : Int = 0
    }
}


/** Charge l'image dans la WebView
 * @param imageWebView WebView destinataire
 * @param src url source de l'image à envoyer dans la WebView destinataire */
fun loadImage(imageWebView: WebView, src: String) {
//    Log.v("ImageHandler", src)
    val a = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=a5f603e1a43101eb53d6bdebac6605aaw4874&projectId=12&idPianoWeek=25&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=382&height=690&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1694954684416&displayConfId=15"
    val html = "<html style=\"height: 100%;\"><head><meta name=\"viewport\" content=\"width=device-width, minimum-scale=0.1\"><title>imageEt (382×690)</title></head><body style=\"margin: 0px; height: 100%; background-color: rgb(14, 14, 14);\"><img style=\"display: block;-webkit-user-select: none;max-width: 100%;margin: auto;background-color: hsl(0, 0%, 90%);\" src='$src'></body></html>"
//    imageWebView.loadData(html, "text/html; charset=utf-8", "UTF-8")
    imageWebView.loadUrl(src)

}

/*private class SetupImageTask(webView: WebView) : AsyncTask<URL?, Int?, Long>() {
    *//*protected fun doInBackground(vararg urls: URL) {
        }*//*
    val webView = webView
    override fun doInBackground(vararg p0: URL?): Long {
        Log.v("async", "heyho")
        webView.evaluateJavascript("setTimeout(function() {${getImageResource + "getImgRes()"}}, 1000)") {
//            val url = it.toString().subSequence(1, it.length-1).toString()
            Log.d("net", it)
//            imageWebView.loadUrl(url)
        }
        return 0
    }
    *//*protected override fun onProgressUpdate(vararg progress: Int) {
        setProgressPercent(progress[0])
    }*//*

    *//*override fun onPostExecute(result: Long) {
        showDialog("Downloaded $result bytes")
    }*//*
}*/
/*val button = findViewById<Button>(R.id.button)
button.setOnClickListener {
    Log.v("scraper", "fetching...")*/
//            scrapeedt()
/*ContentScrapper.getHTMLData(this,"https://edt.grenoble-inp.fr/2023-2024/etudiant/prepaINPValence",object : ContentScrapper.ScrapListener{
    override fun onResponse(html: String?) {
        if(html != null) {
            Toast.makeText(it.context ,html, Toast.LENGTH_LONG).show()
            Log.v("Scraping", html)
        } else {
            Toast.makeText(it.context,"Not found",Toast.LENGTH_LONG).show()
            Log.v("Scraping", "not found")
        }
    }
})*/

//            GetMainHtml.main(this)

//            val token = fetchToken("https://edt.grenoble-inp.fr/2023-2024/etudiant/prepaINPValence")
//            Log.v("Scraper", token)


/*val SDK_INT = Build.VERSION.SDK_INT
if (SDK_INT > 8) {
    val policy = ThreadPolicy.Builder()
        .permitAll().build()
    StrictMode.setThreadPolicy(policy)
    //your codes here
    val url = URL("https://edt.grenoble-inp.fr")
    val urlConnection: URLConnection = url.openConnection()
    val inputStream: InputStream = urlConnection.getInputStream()
    Log.v("Checker", inputStream.toString())
}*/
//        }


/*    fun fetchToken(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        // Parse the response to extract the token
        return response.body?.string() ?: ""
    }*/

/*    suspend fun cofunc() {
        val job = coroutineScope{
            delay(5000)
            getDataFromJs("document.getElementsByTagName('frameset')[1].clientWidth", webView)
        }
    }*/
/*    fun getDataFromJs(command: String, webView: WebView) {
        webView.evaluateJavascript(
            "(function() { return $command; })();"
        ) { s -> returnDataFromJs(s) }
    }*/
/*    fun returnDataFromJs(data: String?) {
        // Do something with the result.
        Log.v("Resizing", data.toString())
    }*/