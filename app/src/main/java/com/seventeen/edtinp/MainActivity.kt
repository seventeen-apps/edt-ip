package com.seventeen.edtinp


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.seventeen.mywardrobe.DatePicker
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Calendar
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), DatePicker.OnDatePass {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var backgroundWebView: WebView
    lateinit var imageWebView: WebView

    val mainUrl = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

    var imgsrc = "https://via.placeholder.com/120x120&text=image1"
//        "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=dbb76b76e990cedcbb0ff47422fb9f0aw14553&projectId=12&idPianoWeek=4&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=382&height=690&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1694188961792&displayConfId=15"

    val scope = CoroutineScope(Dispatchers.IO)

    val imgtest = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=3597fe08f16acc0775c1bbf89879b9e2w10859&projectId=12&idPianoWeek=6&idPianoDay=0%2C1%2C2%2C3%2C4%2C5%2C6&idTree=13808%2C13807&width=1140&height=572&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1695209881600&displayConfId=15"

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


        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())

        val imageView: ImageView = findViewById(R.id.imageView)

        val imageHandler = ImageHandler(this, imageView, myExecutor, myHandler)


        imageWebView = findViewById(R.id.imageWebView)
        imageWebView.settings.javaScriptEnabled = true
        imageWebView.visibility = View.INVISIBLE
        imageWebView.setBackgroundColor(Color.TRANSPARENT)

        backgroundWebView = findViewById(R.id.backgroundWebView)
        backgroundWebView.settings.javaScriptEnabled = true
        backgroundWebView.addJavascriptInterface(WebViewJavaScriptInterface(this, backgroundWebView, imageWebView, imageHandler), "app");
        backgroundWebView.visibility = View.INVISIBLE //TODO: mettre invisible


        //Setup date picker
        val date_button = findViewById<ImageView>(R.id.calendar_button)
        date_button.setOnClickListener {
            val datePicker =
                DatePicker(this, imageHandler)
            datePicker.show(supportFragmentManager, datePicker.TAG)
        }




        // When Button is clicked, executor will
        // fetch the image and handler will display it.
        // Once displayed, it is stored locally



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


                        // Met l'image dans une ImageView

                        imageHandler.setImage(spliturl.joinToString("&"))
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


                        // Met l'image dans une ImageView

                        imageHandler.setImage(spliturl.joinToString("&"))

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
        //Get the menu icon
        val hamButton: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_baseline_menu_24)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        //Set the menu icon as navigation button
        toolbar.navigationIcon = hamButton


        menuInflater.inflate(R.menu.menu, menu)
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        classeTextView.text = DataHandler.data.classe
        return true
    }
    //
    //TODO Documenter
    override fun onDatePass(weekId: String, activity: FragmentActivity?) {
        Log.v("TempSave", "Updated to ${weekId}")
        selectedWeekId = weekId.toInt()
        /*displayedWeekId = selectedWeekId.toInt()
        Log.v("Date Handler", "Moving to week $displayedWeekId")

        // Charge l'image dans la WebView principale
        val spliturl = referenceURL.split("&") as MutableList
        spliturl[idSemaineUrl] = "idPianoWeek=$displayedWeekId"
        loadImage(imageWebView, spliturl.joinToString("&"))*/
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
        */(private val context: Context, private val backgroundWebView: WebView, private val imageWebView: WebView, private val imageHandler: ImageHandler) {

        private var lastUrl = ""

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
                imageHandler.setImage(src)
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
                makeToast("Connection établie")
                imageHandler.setImage(referenceURL)
            }
        }




        //TODO documenter
        @JavascriptInterface
        fun onLoadingFail() {
            makeToast("Chargement échoué")
        }
    }

    companion object {
        var referenceURL : String = ""
        var idSemaineUrl : Int = 0
        var selectedWeekId : Int = 0
        var displayedWeekId : Int = 0
    }
}


class ImageHandler
    (private val context: Context, private val imageView: ImageView, private val executor: ExecutorService, private val handler: Handler) {
    fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context , "Saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }

    fun mLoad(string: String): Bitmap? {
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


    fun setImage(url: String) {
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












/** Charge l'image dans la WebView
 * @param imageWebView WebView destinataire
 * @param src url source de l'image à envoyer dans la WebView destinataire */
fun loadImage(imageWebView: WebView, src: String) {
    Log.v("ImageHandler", src)
//    imageWebView.loadUrl(src)
//    val a = "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=a5f603e1a43101eb53d6bdebac6605aaw4874&projectId=12&idPianoWeek=25&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=382&height=690&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1694954684416&displayConfId=15"
//    val html = "<html style=\"height: 100%;\"><head><meta name=\"viewport\" content=\"width=device-width, minimum-scale=0.1\"><title>imageEt (382×690)</title></head><body style=\"margin: 0px; height: 100%; background-color: rgb(14, 14, 14);\"><img style=\"display: block;-webkit-user-select: none;max-width: 100%;margin: auto;background-color: hsl(0, 0%, 90%);\" src='$src'></body></html>"
//    imageWebView.loadData(html, "text/html; charset=utf-8", "UTF-8")
//    imageWebView.evaluateJavascript(download_image + "downloadImage('$src');") {
//        Log.v("it", it)
//    }

}

//TODO Documenter
/*public fun loadWeek(weekId : Int) {

    // Charge l'image dans la WebView principale
    val spliturl = MainActivity.referenceURL.split("&") as MutableList
    spliturl[MainActivity.idSemaineUrl] = "idPianoWeek=$weekId"
    loadImage(imageWebView, spliturl.joinToString("&"))
}*/

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