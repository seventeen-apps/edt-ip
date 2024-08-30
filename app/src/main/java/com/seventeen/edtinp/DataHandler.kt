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

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.util.Calendar

class DataHandler(private val context: Context) {
    //Setup the data storing file
    private val dataFile = "Data.txt"
    private val dataVersionFile = "DataVersion.txt"
    private val dataVersion: String = "1.2"
    private var data: DataClass = DataClass(dataVersion, "", 0, 0, "", listOf(0, 0), "", false)


    init {
        val defaultData = DataClass(dataVersion, "CPPV", 0, 0, "", listOf(0, 0), context.getString(R.string.PINPV_1A), false)
        setupFile(dataFile, Json.encodeToString(defaultData))
//        editFile(dataFile, Json.encodeToString(defaultData))

        val extractedData = openFile(dataFile)
        if (dataVersion != extractedData.split(",")[0].split(":")[1].replace(""""""", "")) {
            Log.v("DataHandler", "Found outdated data file (version ${extractedData.split(",")[0].split(":")[1].replace(""""""", "")} found, expected ${dataVersion.toFloat()}), reset to default")
            editFile(dataFile, Json.encodeToString(defaultData))
            editFile(dataVersionFile, dataVersion)
            data = defaultData
        } else {
            data = Json.decodeFromString(extractedData)
            if (data.treeId == "") {
                setTreeId(context.getString(R.string.PINPV_1A))
            }
        }

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
        data = DataClass(dataVersion, data.ecole, calendar.get(Calendar.DAY_OF_WEEK) - 1, currentWeekId, "", listOf(0, 0), "", false)

        editFile(dataFile, Json.encodeToString(data))

        Toast.makeText(context, "Mémoire mise à jour :\n ecole ${data.ecole} \n jour ${calendar.get(Calendar.DAY_OF_WEEK) - 1} \n semaine $currentWeekId", Toast.LENGTH_SHORT).show()
        Log.v("DataHandler", "Memory updated :\n class ${data.ecole} \n day ${calendar.get(Calendar.DAY_OF_WEEK) - 1} \n week $currentWeekId")
    }

    fun setCurrentDayOfWeek(day: Int) {
        data.currentDayOfWeek = day
        updateSave()
    }

    fun setCurrentWeekId(id: Int) {
        data.currentWeekId = id
        updateSave()
    }

    /**
     * @return Id de la semaine actuelle
     */
    fun getCurrentWeekId(): Int {
        return data.currentWeekId
    }

    fun setSchool(ecole: String) {
        data.ecole = ecole
        updateSave()
    }

    fun getSchool(): String {
        return data.ecole
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

    fun setId(id: String) {
        data.identifier = id
        updateSave()
        Log.v("DataHandler", "Saved id ${id}")
    }

    fun getId(): String {
        return if (data.identifier == null) {
            ""
        } else {
            data.identifier
        }
    }

    fun setTreeId(treeId: String) {
        if (treeId == "") {
            Toast.makeText(context, "Id vide reçu, réinitialisation de la classe séléctionnée", Toast.LENGTH_LONG).show()
            Log.v("DataHandler", "Got null id !")
            data.treeId = context.getString(R.string.PINPV_1A)
        } else {
            data.treeId = treeId
        }
        updateSave()
        Log.v("DataHandler", "Changing tree id to $treeId")
    }

    fun getTreeId(): String {
        return data.treeId
    }
}
