package com.seventeen.edtinp

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Calendar

class DataHandler(private val context: Context) {
    //Setup the data storing file
    private val dataFile = "Data.txt"
    private var data: DataClass = DataClass("2A-PINP", 0, 0, mutableMapOf("1A-PINP" to "","2A-PINP" to "", "HN1-PINP" to "", "HN2-PINP" to "", "HN3-PINP" to ""), listOf(0, 0), false)


    init {
        val defaultData = DataClass("2A-PINP", 0, 0, mutableMapOf("1A-PINP" to "","2A-PINP" to "", "HN1-PINP" to "", "HN2-PINP" to "", "HN3-PINP" to ""), listOf(0, 0), false)
        setupFile(dataFile, Json.encodeToString(defaultData))
//        editFile(dataFile, Json.encodeToString(defaultData))
        data = Json.decodeFromString<DataClass>(openFile(dataFile))
        Log.v("FileManager", "Loaded: ${Json.encodeToString(data)}")
    }

    /**
     * Find or create file if non-existent
     * @param filename Name of the file
     * @param content String-formatted content
     */
    private fun setupFile(filename: String, content: String) {
        //Try to read the file
        try {
            openFile(filename, true)
            Log.v("FileManager", "$filename found")
        }
        //If file unreadable/unreachable, create a new one
        catch (e: Exception) {
            Log.v("FileManager", "$filename not found")
            createFile(filename, content)

        } finally {
            //Log.v("FileManager", "Setup terminated, $filename at ${getDir(filename, Context.MODE_PRIVATE)}")
        }
    }

    /**
     * Edit the file
     * @param filename Name of the file
     * @param content String-formatted content to write in the file
     */
    fun editFile(filename: String, content: String) {
        context.openFileOutput(filename, Context.MODE_PRIVATE)
            .use { it.write(content.toByteArray()) }
    }

    /**
     * Update the save file
     */
    fun updateSave() {
        context.openFileOutput(dataFile, Context.MODE_PRIVATE)
            .use { it.write(Json.encodeToString(data).toByteArray()) }
    }

    /**
     * Opens a file and return its content, otherwise throws an error
     * @param filename Name of the file
     * @param test Whether or not a log should be sent
     */
    fun openFile(filename: String, test: Boolean = false): String {
        if (!test) {
            Log.v("FileManager", "Opened $filename")
        }; return File(context.filesDir, filename).readText()
    }

    /***
     * Creates a file with specified name and String-formatted content
     * @param filename Name of the file
     * @param content String-formatted content
     */
    private fun createFile(filename: String, content: String) {
        Log.v("FileManager", "Created $filename"); context.openFileOutput(
            filename,
            Context.MODE_PRIVATE
        ).use { it.write(content.toByteArray()) }
    }

    fun flushData() {
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
        data = DataClass(data.classe, calendar.get(Calendar.DAY_OF_WEEK) - 1, currentWeekId, mutableMapOf("1A-PINP" to "","2A-PINP" to "", "HN1-PINP" to "", "HN2-PINP" to "", "HN3-PINP" to ""), listOf(0, 0), false)

        editFile(dataFile, Json.encodeToString(data))

        Toast.makeText(context, "Mémoire mise à jour :\n classe ${data.classe} \n jour ${calendar.get(Calendar.DAY_OF_WEEK) - 1} \n semaine $currentWeekId", Toast.LENGTH_SHORT).show()
        Log.v("DataHandler", "Memory updated :\n class ${data.classe} \n day ${calendar.get(Calendar.DAY_OF_WEEK) - 1} \n week $currentWeekId")
    }

    fun setCurrentDayOfWeek(day: Int) {
        data.currentDayOfWeek = day
        updateSave()
    }

    fun setCurrentWeekId(id: Int) {
        data.currentWeekId = id
        updateSave()
    }

    fun getCurrentWeekId(): Int {
        return data.currentWeekId
    }

    fun setClass(classe: String) {
        data.classe = classe
        updateSave()
    }

    fun getClass(): String {
        return data.classe
    }

    fun setDimensions(dimensions: List<Int>) {
        data.dimensions = dimensions
        updateSave()
    }

    fun getDimensions(): List<Int> {
        return data.dimensions
    }

    fun setLoggingState(state: Boolean) {
        data.loggingState = state
        updateSave()
    }

    fun getLoggingState(): Boolean {
        return data.loggingState
    }

    fun setId(classe: String, id: String) {
        data.identifiers[classe] = id
        updateSave()
        Toast.makeText(context, "Identifiant sauvegardé ${id}", Toast.LENGTH_SHORT).show()
        Log.v("DataHandler", "Saved id ${id}")
    }

    fun getId(): String {
        return if (data.identifiers[getClass()] == null) {
            ""
        } else {
            data.identifiers[getClass()]!!
        }
    }
}
