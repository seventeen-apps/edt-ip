package com.seventeen.edtinp


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var webView: WebView
    val mainUrl = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Add the toolbar
        setSupportActionBar(findViewById(R.id.toolbar))




        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true

        // On agrandit la taille du webView pour optimiser l'affichage
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        Log.d("WebViewHandler", "Gained pixels: ${width * 18 / 100}")
        webView.layoutParams.width = width + width * 18 / 100 + 25

        DataHandler().setup(this)

        // Obtention de l'id de la semaine actuelle
        val calendar = Calendar.getInstance()
        val current_week_number = calendar.get(Calendar.WEEK_OF_YEAR)
        var current_week_id = 0
        if (current_week_number < 32) {
            current_week_id = current_week_number + 20
        } else {
            current_week_id = current_week_number - 32
        }
        var displayed_week_id = current_week_id
        Log.v("Date Handler", "Week number is $current_week_number week id is $current_week_id")


        // Redéfinition de la méthode onPageFinished
        webView.webViewClient = object : WebViewClient() {
            var isRedirected = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (!isRedirected) {
                    Log.v("URL Loader", "Loading $url")
                    //Do something you want when starts loading
                }
                isRedirected = false
            }

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
                    webView.evaluateJavascript(jsCode, null)
                }
                // Vérification de la semaine affichée
                webView.evaluateJavascript("$getFromPage") {
                    var cache_week_number = ""
                    if ((it == "null") or (it.length < 5)) {
                        Log.d("Preloader", "Got null resource")
                    } else {
                        cache_week_number = it.subSequence(2, 4).toString()
                        if (cache_week_number[1].toString() == " ") {
                            cache_week_number = cache_week_number[0].toString()
                        }
                        Log.d("Scraping", "${cache_week_number.toInt()} ${current_week_number}")
                        if (cache_week_number.toInt() != current_week_number) {
                            Log.d("Preloader", "Found cached week, reloading to ${current_week_id}")
                            webView.evaluateJavascript(
                                ("setTimeout(function() {${js_functions + "push($current_week_id, true)"}}, 1000)"),
                                null
                            )
                            displayed_week_id = current_week_id
                        }
                    }
                }

            }
        }


        webView.loadUrl(mainUrl)


        val prevButton = findViewById<Button>(R.id.prev_week)
        val nextButton = findViewById<Button>(R.id.next_week)

        prevButton.setOnClickListener {
            if (displayed_week_id > 0) {
                val jsCode = (js_functions + "push(${displayed_week_id - 1}, true)")
                displayed_week_id -= 1
                webView.evaluateJavascript(jsCode, null)
            }
        }
        nextButton.setOnClickListener {
            if (displayed_week_id < 51) {
                val jsCode = (js_functions + "push(${displayed_week_id + 1}, true)")
                displayed_week_id += 1
                Log.v("Date Handler", "Moving to week $displayed_week_id")
                webView.evaluateJavascript(jsCode, null)
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
        webView.evaluateJavascript(getFromPage) {
            if ((it == "null") or (it.length < 5)) {
                Log.d("Preloader", "Got null resource"); webView.loadUrl(mainUrl)
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
                    ("${preload + search + load}")
                webView.evaluateJavascript(jsCode, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }
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