package com.seventeen.edtinp



import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    lateinit var webView: WebView
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Add the toolbar
        setSupportActionBar(findViewById(R.id.toolbar))


        val url = "https://edt.grenoble-inp.fr/2023-2024/exterieur"

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true

        // On agrandit la taille du webView pour optimiser l'affichage
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        Log.d("WebViewHandler", "Gained pixels: ${width*18/100}")
        webView.layoutParams.width = width + width*18/100 + 25

        DataHandler().setup(this)

        // Obtention de l'id de la semaine actuelle
        val calendar = Calendar.getInstance()
        var current_week = calendar.get(Calendar.WEEK_OF_YEAR)
        if (current_week < 32) {
            current_week += 20
        } else {
            current_week -= 32
        }


        Log.v("Date Handler", "Week is $current_week")


        // Redéfinition de la méthode onPageFinished
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String) {
                super.onPageFinished(view, url)

                Log.v("URL Loader", url)
//                webView.evaluateJavascript("var framesets = document.getElementsByTagName('frameset')[1];", null)
                // Mise en page du contenu

                var preload = ""
                when (DataHandler.data.classe) {
                    "1A-PINP" -> preload = preload_1A
                    "2A-PINP" -> preload = preload_2A
                }
                val jsCode =
                    ("setTimeout(function() {${cleanup + preload + setup_saturday + setup_sunday}}, 100)")
                webView.evaluateJavascript(jsCode, null)



                // Vérification de la semaine affichée
                webView.evaluateJavascript(getFromPage) {
                    val cache_week_str = it.subSequence(2, 4).toString()
                    var current_week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
                    if (cache_week_str != "ll") {
                        if (cache_week_str.toInt() != current_week) {
                            Log.d("Preloader", "Found cached week, reloading...")
                            if (current_week < 32) {
                                current_week += 20
                            } else {
                                current_week -= 32
                            }
                            webView.evaluateJavascript(
                                (js_functions + "push(${current_week}, true)"),
                                null
                            )
                        }
                    }
                }
            }
        }


        webView.loadUrl(url)


        val prevButton = findViewById<Button>(R.id.prev_week)
        val nextButton = findViewById<Button>(R.id.next_week)

        prevButton.setOnClickListener {
            val jsCode = (js_functions + "push($current_week-1, true)")
            current_week -= 1
            webView.evaluateJavascript(jsCode, null)
        }
        nextButton.setOnClickListener {
            val jsCode = (js_functions + "push($current_week+1, true)")
            current_week += 1
            webView.evaluateJavascript(jsCode, null)
        }
    }

    fun changeClasse(classe : String) {
        DataHandler.data.classe = classe
        DataHandler().updateSave(this)
    }

    //Setup the menu at the top left corner
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var preload = ""
        when (item.title){
            getString(R.string.A1) -> { DataHandler.data.classe = "1A-PINP"//item.title.toString()
                                        DataHandler().updateSave(this)
                                        Log.v("Menu Handler", DataHandler.data.classe)
                                        preload = preload_1A }

            getString(R.string.A2) -> { DataHandler.data.classe = "1A-PINP"//item.title.toString()
                                        DataHandler().updateSave(this)
                                        Log.v("Menu Handler", DataHandler.data.classe)
                                        preload = preload_2A }
        }
        webView.evaluateJavascript(getFromPage) {
            val selectedWeek = it.subSequence(2, 4).toString().toInt()
            val jsCode =
                ("setTimeout(function() {${cleanup + preload + setup_saturday + setup_sunday}}, 100)")
            webView.evaluateJavascript(jsCode, null)
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