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


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.Executors
import kotlin.concurrent.thread


//TODO ajouter la possibilité d'afficher uniquement le jour J

class MainActivity : AppCompatActivity(), DatePicker.OnDatePass,
    NavigationView.OnNavigationItemSelectedListener {
    @SuppressLint("SetJavaScriptEnabled")

    lateinit var backgroundWebView: WebView
    lateinit var foregroundWebView: WebView
    lateinit var imageView: ImageView
    val mainUrl = "https://edt.grenoble-inp.fr/" + scholarYear + "/exterieur"

    val jsSetReferenceDelay = 1200
    val jsSetBitmapDelay = 100

    private lateinit var dataHandler: DataHandler
    private lateinit var cacheHandler: CacheHandler
    private lateinit var imageHandler: ImageHandler
    private lateinit var classHandler: ClassHandler

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Ajoute la barre d'outil supérieure
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialise le tiroir d'écoles
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        // Initialisation des paramètres de la classe ImageHandler, qui est chargée de l'affichage de l'edt
        imageView = findViewById(R.id.imageView)
        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())
        dataHandler = DataHandler(this) // Initialise le gestionnaire de données
        cacheHandler = CacheHandler(this, dataHandler)
        imageHandler = ImageHandler(this, dataHandler, cacheHandler)
        classHandler = ClassHandler(this, dataHandler)

        // Initialisation de la WebView d'arrière plan nécessaire pour générer le lien de référence
        backgroundWebView = findViewById(R.id.backgroundWebView)
        backgroundWebView.settings.javaScriptEnabled = true
        backgroundWebView.addJavascriptInterface(BackgroundWebViewJavaScriptInterface(this, imageHandler, dataHandler, cacheHandler), "app")


        // Initialisation de la WebView intermédiaire nécessaire pour générer des images à partir de l'url obtenue en amont
        foregroundWebView = findViewById<WebView>(R.id.foregroundWebView)
        foregroundWebView.settings.javaScriptEnabled = true
        foregroundWebView.setBackgroundColor(Color.argb(1, 0, 0, 0))
        foregroundWebView.addJavascriptInterface(ForegroundWebViewJavaScriptInterface(this, imageHandler, dataHandler), "app")


        // Initialisation du calendrier de sélection de date
        val dateButton = findViewById<ImageView>(R.id.calendar_button)
        dateButton.setOnClickListener {
            val datePicker = DatePicker(imageHandler, dataHandler)
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


        // Désactive les boutons par défaut
        switchNavigation(this@MainActivity, navigationValue = false)
        val prevButton = findViewById<Button>(R.id.prev_week)
        val nextButton = findViewById<Button>(R.id.next_week)
        val refreshButton = findViewById<ImageView>(R.id.refresh_button)
        // Affiche l'image de la semaine gardée en cache
        var imageBitmap = cacheHandler.getImage("week$displayedWeekId${dataHandler.getTreeId()}")
        if (imageBitmap != null) {
            Log.v("CacheHandler", "Displaying cached image")
            imageView.setImageBitmap(imageBitmap)
            if (cacheHandler.getImage("week${displayedWeekId + 1}${dataHandler.getTreeId()}") != null) {
                findViewById<Button>(R.id.next_week).isEnabled = true
            }
        }

        // Supprime l'image de la semaine précédente gardée en cache
        if (cacheHandler.clearOutdatedCache()) { Toast.makeText(this, "Images en cache effacées", Toast.LENGTH_SHORT).show(); Log.v("CacheHandler", "Outdated pictures deleted") }

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
//                if (!isRedirected) {
                Log.v("URLLoader", "Loading $url")
                failChecking = true
                backgroundWebView.evaluateJavascript("setTimeout(function() { app.checkFail() }, 15000)", null)
//                }
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
                    Log.v("URLLoader", "Redirected from $url")

                    if (url == mainUrl) {
                        findViewById<ProgressBar>(R.id.progressBar).setProgress(20, true)
                    }
                }

                // Mise en page du contenu
                if (("plannings" in url)) {
                    Log.v("URLLoader", "Loading page")
                    var search = ""
                    when (dataHandler.getSchool()) {
                        "1A-PINP" -> search = search1A
                        "2A-PINP" -> search = search2A
                        "HN1-PINP" -> search = searchHN1
                        "HN2-PINP" -> search = searchHN2
                        "HN3-PINP" -> search = searchHN3
                        else -> search = search1A
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
                            findViewById<ProgressBar>(R.id.progressBar).setProgress(60, true)
                            cacheWeekNumber = it.subSequence(2, 4).toString()
                            if (cacheWeekNumber[0].toString() == " ") {
                                cacheWeekNumber = cacheWeekNumber[0].toString()
                            }
                            cacheWeekNumber = cacheWeekNumber.replace(" ", "")
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

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError) {
                Log.d("URLLoader", "${error.errorCode} : ${error.description}")
                if (error.errorCode == 500) {
                    Toast.makeText(this@MainActivity, "Accès au site refusé, essayez de réinstaller l'application ou redémarrez le téléphone. ", Toast.LENGTH_LONG).show()
                    Log.d("URLLoader", "Error 500 : ${error.description}")
                }
            }
        }

        prevButton.setOnClickListener {
            if (!navigationState) {
                displayedWeekId = dataHandler.getCurrentWeekId()
                Log.v("Date Handler", "Moving to week $displayedWeekId")
                imageView.setImageBitmap(cacheHandler.getImage("week${displayedWeekId}${dataHandler.getTreeId()}"))
                prevButton.isEnabled = false
                if (cacheHandler.getImage("week${displayedWeekId + 1}${dataHandler.getTreeId()}") != null) {
                    nextButton.isEnabled = true
                }
            } else {
                backgroundWebView.evaluateJavascript(check_edt_availability) {// Nécessaire pour invalider le clic si page non chargée
                    if (it != "null") {
                        val result =
                            it.toInt() // Taille de la recherche du tag img dans la fenêtre, 1 si edt visible, 0 sinon

                        // Incrémentation de l'id de la semaine
                        if ((displayedWeekId > 0) and (result != 0)) { // result == 0 => edt non visible
                            displayedWeekId -= 1
                            Log.v("Date Handler", "Moving to week $displayedWeekId")
                            // Met l'image dans une ImageView
                            imageHandler.updateForegroundWebView()
                        }
                    }
                }
            }
        }
        nextButton.setOnClickListener {
            if (!navigationState) {
                displayedWeekId = dataHandler.getCurrentWeekId() + 1
                Log.v("Date Handler", "Moving to week $displayedWeekId")
                imageView.setImageBitmap(cacheHandler.getImage("week${displayedWeekId}${dataHandler.getTreeId()}"))
                prevButton.isEnabled = true
                nextButton.isEnabled = false
            } else {
                backgroundWebView.evaluateJavascript(check_edt_availability) {// Nécessaire pour invalider le clic si page non chargée
                    if (it != "null") {
                        val result =
                            it.toInt() // Taille de la recherche du tag img dans la fenêtre, 1 si edt visible, 0 sinon

                        // Incrémentation de l'id de la semaine
                        if ((displayedWeekId < 51) and (result != 0)) { // Result == 0 => edt non visible
                            displayedWeekId += 1
                            Log.v("Date Handler", "Moving to week $displayedWeekId")
                            // Met à jour l'image
                            imageHandler.updateForegroundWebView()
                        }
                    }
                }
            }
        }

        // Initialisation du bouton de rafraîchissement
        refreshButton.setOnClickListener {
            thread {
                runOnUiThread { switchNavigation(this@MainActivity, navigationValue = false) }
                if (isOnline()) {
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                        findViewById<ProgressBar>(R.id.progressBar).setProgress(10, true)
                        backgroundWebView.loadUrl(mainUrl)
                    }
                } else {
                    // Affiche un dialogue et propose de rester en mode hors ligne
                    //TODO ajouter une checkbox pour ne plus afficher le dialogue
                    this.runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Pas de connexion internet")
                            .setMessage("Vous pouvez quand même consulter les semaines en cache, fermer l'application ?")
                            .setPositiveButton("Fermer l'application") { dialog, _ ->
                                dialog.dismiss(); finishAndRemoveTask()
                            }
                            .setNegativeButton("Rester") { dialog, _ ->
                                dialog.dismiss()
                                runOnUiThread {
                                    switchNavigation(this@MainActivity, navigationValue = false, loadFailure = true)
                                    if (cacheHandler.getImage("week${displayedWeekId+1}${dataHandler.getTreeId()}") != null) {
                                        findViewById<Button>(R.id.next_week).isEnabled = true
                                    } else if (cacheHandler.getImage("week${displayedWeekId - 1}${dataHandler.getTreeId()}") != null) {
                                        findViewById<Button>(R.id.prev_week).isEnabled = true
                                    }
                                }
                            }
                            .setOnCancelListener {
                                it.dismiss()
                                runOnUiThread {
                                    switchNavigation(this@MainActivity, navigationValue = false, loadFailure = true)
                                    if (cacheHandler.getImage("week${displayedWeekId+1}${dataHandler.getTreeId()}") != null) {
                                        findViewById<Button>(R.id.next_week).isEnabled = true
                                    } else if (cacheHandler.getImage("week${displayedWeekId - 1}${dataHandler.getTreeId()}") != null) {
                                        findViewById<Button>(R.id.prev_week).isEnabled = true
                                    }
                                }
                            }
                            .show()
                    }
                }
            }
        }

        cacheHandler.listCache()

        thread {
            // Vérification de la connectivité à internet
            if (isOnline()) {
                // Charge la WebView d'arrère-plan
                runOnUiThread {backgroundWebView.loadUrl(mainUrl); findViewById<ProgressBar>(R.id.progressBar).setProgress(10, true) }
            } else {
                // Affiche un dialogue et propose de rester en mode hors ligne
                //TODO ajouter une checkbox pour ne plus afficher le dialogue
                this.runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("Pas de connexion internet")
                        .setMessage("Vous pouvez quand même consulter les semaines en cache, fermer l'application ?")
                        .setPositiveButton("Fermer l'application") { dialog, _ ->
                            dialog.dismiss(); finishAndRemoveTask()
                        }
                        .setNegativeButton("Rester") { dialog, _ ->
                            dialog.dismiss()
                            runOnUiThread {
                                switchNavigation(this@MainActivity, navigationValue = false, loadFailure = true)
                                if (cacheHandler.getImage("week${displayedWeekId+1}${dataHandler.getTreeId()}") != null) {
                                    findViewById<Button>(R.id.next_week).isEnabled = true
                                } else if (cacheHandler.getImage("week${displayedWeekId - 1}${dataHandler.getTreeId()}") != null) {
                                    findViewById<Button>(R.id.prev_week).isEnabled = true
                                }
                            }
                        }
                        .setOnCancelListener {
                            it.dismiss()
                            runOnUiThread {
                                switchNavigation(this@MainActivity, navigationValue = false, loadFailure = true)
                                if (cacheHandler.getImage("week${displayedWeekId+1}${dataHandler.getTreeId()}") != null) {
                                    findViewById<Button>(R.id.next_week).isEnabled = true
                                } else if (cacheHandler.getImage("week${displayedWeekId - 1}${dataHandler.getTreeId()}") != null) {
                                    findViewById<Button>(R.id.prev_week).isEnabled = true
                                }
                            }
                        }
                        .show()
                }
            }
        }
    }

    fun openCloseNavigationDrawer(view: View) {
        if (findViewById<DrawerLayout>(R.id.drawer_layout).isDrawerOpen(GravityCompat.START)) {
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        } else {
            findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
        }
    }

    // TODO on resume renvoie en navgation limitée avec la possibilité de recharger
    override fun onPause() {
        super.onPause()
        foregroundWebView.stopLoading()
        backgroundWebView.stopLoading()
    }

    override fun onStop() {
        super.onStop()
        foregroundWebView.stopLoading()
        backgroundWebView.stopLoading()
        if (dataHandler.getLoggingState()) {
            LogcatToFile.saveLogcatToFile(this)
        }
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


    /** Initialise le menu en haut à droite en fonction de l'école sélectionnée */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val hamButton: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_baseline_menu_24)
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        // Met l'icône de menu en tant que bouton de navigation
//        toolbar.navigationIcon = hamButton
        var menuId = R.menu.menu_cppv
        when (dataHandler.getSchool()) {
            "ESISAR" -> { menuId = R.menu.menu_esisar }
            "CPPV" -> { menuId = R.menu.menu_cppv }
            "ENSIMAG Ingé" -> { menuId = R.menu.menu_ensimag_ingenieurs }
            "ENSIMAG Masters" -> { menuId = R.menu.menu_ensimag_masters }
            "ENSE3" -> { menuId = R.menu.menu_ense3 }
            "PHELMA" -> { menuId = R.menu.menu_phelma }
        }
        menuInflater.inflate(menuId, menu)
        val classTextView = findViewById<TextView>(R.id.classe_tv)
        // TODO changer pour afficher le nom de la classe
        classTextView.text = dataHandler.getSchool()
        return true
    }

    /** Active ou désactive les champs de sélection en fonction de l'objet isNavigationRestricted */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (!navigationState) {
            for (item in menu!!.children) {
                if (item.title == getString(R.string.menuitem_logs_title)) {
                    item.isEnabled = true
                    item.isChecked = dataHandler.getLoggingState()
                } else { item.isEnabled = false }

            }
        } else {
            for (item in menu!!.children) {
                item.isEnabled = true
                if (item.title == getString(R.string.menuitem_logs_title)) {
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
        var classeId = ""
        when (item.title) {
            /*getString(R.string.A1) -> {
//                changeClasse("1A-PINP")
//                Log.v("Menu Handler", dataHandler.getClass())
                search = search1A

            }

            getString(R.string.A2) -> {
//                changeClasse("2A-PINP")
//                Log.v("Menu Handler", dataHandler.getClass())
                search = search2A
            }

            getString(R.string.HN1) -> {
//                changeClasse("HN1-PINP")
//                Log.v("Menu Handler", dataHandler.getClass())
                search = searchHN1
            }

            getString(R.string.HN2) -> {
//                changeClasse("HN2-PINP")
//                Log.v("Menu Handler", dataHandler.getClass())
                search = searchHN2
            }

            getString(R.string.HN3) -> {
//                changeClasse("HN3-PINP")
//                Log.v("Menu Handler", dataHandler.getClass())
                search = searchHN3
            }*/
//            "Effaçage général" -> { flushCache(applicationContext, true) }
//            "Effaçage simple" -> { flushCache(applicationContext) }
//            "Effaçer le fichier de sauvegarde" -> { dataHandler.flushData() }
            getString(R.string.menuitem_logs_title) -> { item.isChecked = !item.isChecked; dataHandler.setLoggingState(item.isChecked) }
            else -> {
                if ((item.title != null) or (item.title != "")) {
                    classHandler.switchClass(item.title.toString())
                }
            }
        }
        /*if ((item.title != null) or (item.title != "")) {
            classHandler.switchClass(item.title.toString())
        }*/

        // Met le nom de la classe à jour
        val classeTextView = findViewById<TextView>(R.id.classe_tv)
        //TODO Changer ça aussi
        if (item.title != getString(R.string.menuitem_logs_title)) { classeTextView.text = item.title }


        // En cas de besoin, choisir une classe recharge la page (permet de sortir d'un bug)
        //TODO nécéssaire ?
        if (item.title !in arrayOf("Effaçage général", "Effaçage simple", "Effaçer le fichier de sauvegarde", getString(R.string.menuitem_logs_title))) {
            runOnUiThread {
                switchNavigation(this@MainActivity, navigationValue = false)
                imageHandler.updateForegroundWebView()
            }
            /*backgroundWebView.evaluateJavascript(get_selected_week) {


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
            }*/
        }
        return super.onOptionsItemSelected(item)
    }

    /** Callback appelé lors d'un click sur un champ du menu déroulant à gauche */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var newClassName = ""
        when (item.title) {

            getString(R.string.nav_esisar) -> { newClassName = getString(R.string.ESISAR_1A_name); invalidateOptionsMenu() }

            getString(R.string.nav_pinpv) -> { newClassName = getString(R.string.PINPV_1A_name); invalidateOptionsMenu() }

            getString(R.string.nav_ensimag_ingenieurs) -> { newClassName = getString(R.string.ENSIMAG_INGENIEURS_1A_g1g2_name); invalidateOptionsMenu() }

            getString(R.string.nav_ensimag_masters) -> { newClassName = getString(R.string.ENSIMAG_MASTERS_CODAS1_name); invalidateOptionsMenu() }

            getString(R.string.nav_ense3) -> { newClassName = getString(R.string.ENSE3_1A_ETU_A_name); invalidateOptionsMenu() }

            getString(R.string.nav_phelma) -> { newClassName = getString(R.string.PHELMA_1A_AP_MEP_name); invalidateOptionsMenu() }

        }
        if (newClassName != "") {
            if (classHandler.switchClass(newClassName)) {
                imageHandler.updateForegroundWebView()
            }
        }
        return true
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
        private val dataHandler: DataHandler,
        private val cacheHandler: CacheHandler
    ) {
        @JavascriptInterface
        fun makeToast(message: String?, lengthLong: Boolean = true) {
            Toast.makeText(context, message, if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun checkFail() {
            if (failChecking) {
                Log.v("FailHandler", "Failed to load the page")
//                makeToast("Contacter les serveurs prend un temps anormal, les serveurs sont peut-être inaccessibles. Essayez de relancer/réactiver le wifi/données.", true)
                context.runOnUiThread {
                    context.findViewById<WebView>(R.id.backgroundWebView).stopLoading()
                    context.findViewById<WebView>(R.id.foregroundWebView).stopLoading()
                    context.findViewById<ProgressBar>(R.id.progressBar).setProgress(0, false)
                    switchNavigation(context, navigationValue = false, loadFailure = true)
                    if (cacheHandler.getImage("week${displayedWeekId + 1}${dataHandler.getTreeId()}") != null) {
                        context.findViewById<Button>(R.id.next_week).isEnabled = true
                    } else if (cacheHandler.getImage("week${displayedWeekId - 1}${dataHandler.getTreeId()}") != null) {
                        context.findViewById<Button>(R.id.prev_week).isEnabled = true
                    }
                    AlertDialog.Builder(context)
                        .setTitle("Les serveurs mettent du temps à répondre")
                        .setMessage(
                            "Contacter les serveurs prend un temps anormal, les serveurs sont peut-être inaccessibles. Essayez de relancer/réactiver le wifi/données. \n" +
                                    "\n" +
                                    " Vous pouvez quand même consulter les semaines en cache, fermer l'application ?"
                        )
                        .setPositiveButton("Fermer l'application") { dialog, _ ->
                            dialog.dismiss(); context.finishAndRemoveTask()
                        }
                        .setNegativeButton("Rester") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } else { Log.v("FailHandler", "No failure detected") }
        }

        @JavascriptInterface
        fun setProgress(progress: Int) {
            context.runOnUiThread { context.findViewById<ProgressBar>(R.id.progressBar).setProgress(progress, true) }
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
                //TODO Remplacer par un placeholder
                val spliturl = referenceURL.split("&") as MutableList
                spliturl[idSemaineUrl + 1] = "idPianoDay=0%2C1%2C2%2C3%2C4"
                dataHandler.setDimensions(listOf(splitUrl[widthId].split("=")[1].toInt(), splitUrl[heightId].split("=")[1].toInt()))
                referenceURL = spliturl.joinToString("&")


                // Met à jour l'identifiant si nécéssaire
                var identifiant = ""
                if (referenceURL.split("?")[1].split("=")[0] == "identifier") {
                    identifiant = referenceURL.split("&")[0].split("?")[1].split("=")[1]
                }
                if (((identifiant != "") and (identifiant != dataHandler.getId()))) {
                    dataHandler.setId(identifiant)
                }

                // Met à jour l'identifiant de la classe si nécéssaire
                var treeId = ""
                if (referenceURL.split("&")[idSemaineUrl + 2].split("=")[0] == "idTree") {
                    treeId = referenceURL.split("&")[idSemaineUrl + 2].split("=")[1]
                }
                if (((treeId != "") and (dataHandler.getTreeId() == ""))) {
                    Log.v("URLHandler", "Saved tree id")
                    dataHandler.setTreeId(treeId)
                }
                context.findViewById<ProgressBar>(R.id.progressBar).setProgress(80, true)

                // Charge l'url dans la WebView intermédiaire
//                context.runOnUiThread{ context.findViewById<WebView>(R.id.foregroundWebView).loadUrl(referenceURL) }
                context.runOnUiThread{ imageHandler.updateForegroundWebView() }
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

                savedBitmaps["current"] = false
                failChecking = false

                makeToast("Connexion établie", false)
            }
        }

        /** Fonction appelée en cas d'échec lors de la récupération du lien de référence de l'image */
        @JavascriptInterface
        fun onLoadingFail() {
            makeToast("Chargement arrière échoué")
            AlertDialog.Builder(context)
                .setTitle("Une erreur est survenue")
                .setMessage(
                    "Une petite erreur est survenue, relancer l'application devrait résoudre le problème. \n" +
                            "\n" +
                            " Si le problème persiste, essayez de vider le cache."
                )
                .setPositiveButton("Fermer l'application") { dialog, _ ->
                    dialog.dismiss(); context.finishAndRemoveTask()
                }
                .setNegativeButton("Rester") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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
        private val imageHandler: ImageHandler,
        private val dataHandler: DataHandler
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
            context.findViewById<ProgressBar>(R.id.progressBar).setProgress(100, false)

            val foregroundWebView = context.findViewById<WebView>(R.id.foregroundWebView)

            val bitmap = imageHandler.getBitmapFromWebView()

            // Rétablit la navigation avant de mettre à jour l'ImageView principale
            context.runOnUiThread {
                switchNavigation(context, navigationValue = true)
                context.findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE
                context.findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)
                if (savedBitmaps["current"] == false) {
                    savedBitmaps["current"] = true
                    imageHandler.captureWebViewContent()
                }
                if ((savedBitmaps["next"] == false) and (displayedWeekId == dataHandler.getCurrentWeekId() + 1)) {
                    savedBitmaps["next"] = true
                    imageHandler.captureWebViewContent()
                }
            }
        }


    }

    companion object {
        var referenceURL: String = ""
        var idSemaineUrl: Int = 0
        var selectedWeekId: Int = 0
        var displayedWeekId: Int = 0
        var navigationState: Boolean = false
        var failChecking: Boolean = false
        var savedBitmaps: MutableMap<String, Boolean> = mutableMapOf("current" to true, "next" to false)
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
        context.findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE
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
    if (cacheDir != null && cacheDir.isDirectory) {
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
                if ((weekId == null) or ((weekId != null) and ("week${weekId}" in children[i]))) {
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