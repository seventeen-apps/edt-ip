package com.seventeen.edtinp


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.net.URL
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var backgroundWebView: WebView
    lateinit var imageWebView: WebView

    val mainUrl = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

    var imgsrc = "https://via.placeholder.com/120x120&text=image1"
//        "https://edt.grenoble-inp.fr/2023-2024/exterieur/jsp/imageEt?identifier=dbb76b76e990cedcbb0ff47422fb9f0aw14553&projectId=12&idPianoWeek=4&idPianoDay=0%2C1%2C2%2C3%2C4&idTree=13808%2C13807&width=382&height=690&lunchName=REPAS&displayMode=1057855&showLoad=false&ttl=1694188961792&displayConfId=15"

    val scope = CoroutineScope(Dispatchers.IO)


    suspend fun getImgRes(webView: WebView) = coroutineScope {
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
    }



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Add the toolbar
        setSupportActionBar(findViewById(R.id.toolbar))




        backgroundWebView = findViewById(R.id.backgroundWebView)
        backgroundWebView.settings.javaScriptEnabled = true
        backgroundWebView.visibility = View.VISIBLE

        imageWebView = findViewById(R.id.imageWebView)
        imageWebView.settings.javaScriptEnabled = true
        imageWebView.visibility = View.INVISIBLE

        // On agrandit la taille du webView pour optimiser l'affichage
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        Log.d("WebViewHandler", "Gained pixels: ${width * 18 / 100}")
        backgroundWebView.layoutParams.width = width + width * 18 / 100 + 25

        DataHandler().setup(this)

        // Obtention de l'id de la semaine actuelle
        val calendar = Calendar.getInstance()
        var current_week_number = calendar.get(Calendar.WEEK_OF_YEAR)
        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) or (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
            current_week_number += 1
        }
        val current_week_id: Int
        if (current_week_number < 32) {
            current_week_id = current_week_number + 20
        } else {
            current_week_id = current_week_number - 32
        }
        var displayed_week_id = current_week_id
        Log.v("Date Handler", "Week number is $current_week_number week id is $current_week_id")



        imageWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.v("ImageHandler", "Loading page in webview")
            }
        }


        // Redéfinition des la méthodes de la WebView d'arrière-plan
        backgroundWebView.webViewClient = object : WebViewClient() {
            var isRedirected = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (!isRedirected) {
                    Log.v("URL Loader", "Loading $url")
                    //Do something you want when starts loading
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
                    Log.v("URL Loader", "Redirected from $url")
                }

//                webView.evaluateJavascript("var framesets = document.getElementsByTagName('frameset')[1];", null)
                // Mise en page du contenu
                if (url != mainUrl) {
                    Log.v("URL Loader", "Loading page")
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
//                }
                    // Vérification de la semaine affichée
                    backgroundWebView.evaluateJavascript(getSelectedWeek) {
                        var cache_week_number: String
                        if ((it == "null") or (it.length < 5)) {
                            Log.d("Preloader", "Got null resource")
                        } else {
                            cache_week_number = it.subSequence(2, 4).toString()
                            if (cache_week_number[1].toString() == " ") {
                                cache_week_number = cache_week_number[0].toString()
                            }
                            if (cache_week_number.toInt() != current_week_number) {
                                Log.d(
                                    "Preloader",
                                    "Found cached week, reloading to ${current_week_id}"
                                )
                                displayed_week_id = current_week_id
                            } else {

                            }
                            imageWebView.visibility = View.INVISIBLE



                            Log.v("hey", "I AM HERE")

//                            scope.launch { getImgRes(imageWebView) }
//                            Handler().postDelayed(java.lang.Runnable { SetupImageTask(imageWebView).execute() }, 3000)
//TODO revoir la méthode onPageFinished de zéro pour la restructurer

                            imageWebView.loadUrl(imgsrc)
                        }
                    }

                    /*val jsCode = (js_functions + "push(${displayed_week_id + 1}, true)")
                    backgroundWebView.evaluateJavascript(jsCode) {
    //                            val url = "https://${it.toString().subSequence(9, it.length).toString()}"
                        val url = it.toString().subSequence(1, it.length-1).toString()
                        Log.d("net" ,url)
                        imageWebView.loadUrl(url)
                    }*/

                }
            }

        }


        backgroundWebView.loadUrl(mainUrl)


        val prevButton = findViewById<Button>(R.id.prev_week)
        val nextButton = findViewById<Button>(R.id.next_week)


//TODO désactiver les boutons par défaut et les activer lorsque la page est affichée ?

        prevButton.setOnClickListener {
            if (displayed_week_id > 0) {
                /*val jsCode =
                    (js_functions + "push(${displayed_week_id - 1}, true)" + setup_sunday + setup_saturday)
                displayed_week_id -= 1
                backgroundWebView.evaluateJavascript(jsCode, null)*/
//                webView.evaluateJavascript("setTimeout(function() {${setup_saturday + setup_sunday}}, 1000)", null)
                //TODO
                val jsCode = (checkWeekAvailability)
                backgroundWebView.evaluateJavascript(jsCode) {
                    if (it != "null") {
                        val result = it.toInt()
                        Log.d("ImageHandler", result.toString())
                        if ((displayed_week_id < 51) and (result != 0)) {
                            val jsCode = (js_functions + "push(${displayed_week_id - 1}, true)")
                            displayed_week_id -= 1
                            Log.v("Date Handler", "Moving to week $displayed_week_id")
                            // Charge l'image dans la WebView image
                            backgroundWebView.evaluateJavascript(jsCode) { val url = it.toString().subSequence(1, it.length - 1).toString();Log.d("net", url);imageWebView.loadUrl(url) }
                        }
                    }
                }
            }
        }
        nextButton.setOnClickListener {
            val jsCode = (checkWeekAvailability)
            backgroundWebView.evaluateJavascript(jsCode) {
                if (it != "null") {
                    val result = it.toInt()
                    Log.d("ImageHandler", result.toString())
                    if ((displayed_week_id < 51) and (result != 0)) {
                        val jsCode = (js_functions + "push(${displayed_week_id + 1}, true)")
                        displayed_week_id += 1
                        Log.v("Date Handler", "Moving to week $displayed_week_id")
                        // Charge l'image dans la WebView image
                        backgroundWebView.evaluateJavascript(jsCode) {
//                            val url = "https://${it.toString().subSequence(9, it.length).toString()}"
                            val url = it.toString().subSequence(1, it.length - 1).toString()
                            Log.d("net", url)

                            imageWebView.loadUrl(url)
//                            imageWebView.loadUrl(it.toString().subSequence(9, it.length).toString())
//                            imageWebView.loadUrl("https://${it.toString().subSequence(9, it.length).toString()}")
                            /*imageWebView.evaluateJavascript(getHtml) {
                                Log.d("ImageHandler", it)//TODO
                            }*/
                        }
                    }
                }
            }
        }
    }

    fun changeClasse(classe: String) {
        DataHandler.data.classe = classe
        DataHandler().updateSave(this)
    }

    //Setup the menu at the top left corner
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        classeTextView.text = DataHandler.data.classe
        return true
    }

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
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        classeTextView.text = DataHandler.data.classe
        backgroundWebView.evaluateJavascript(getSelectedWeek) {
            if ((it == "null") or (it.length < 5)) {
                Log.d("Preloader", "Got null resource"); backgroundWebView.loadUrl(mainUrl)
            } else {
                Log.v("DEBUG", "reload")
                /*var selectedWeek = it.subSequence(2, 4).toString()
                if (selectedWeek[1].toString() == " ") { selectedWeek = selectedWeek[0].toString() }
                if (selectedWeek < 32) {
                        selectedWeek += 20
                    } else {
                        selectedWeek -= 32
                    }
                Log.d("Switch", selectedWeek)*/
                val jsCode =
                    (preload + search + load)
                backgroundWebView.evaluateJavascript(jsCode, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

private class SetupImageTask(webView: WebView) : AsyncTask<URL?, Int?, Long>() {
    /*protected fun doInBackground(vararg urls: URL) {
        }*/
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
//TODO la asynctask est peut etre une solution, premièrement réussir à faire une asynctask qui marche
    /*protected override fun onProgressUpdate(vararg progress: Int) {
        setProgressPercent(progress[0])
    }*/

    /*override fun onPostExecute(result: Long) {
        showDialog("Downloaded $result bytes")
    }*/
}
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