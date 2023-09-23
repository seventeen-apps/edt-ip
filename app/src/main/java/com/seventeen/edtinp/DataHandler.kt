package com.seventeen.edtinp

import android.content.Context
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class DataHandler(private val context: Context) {
    //Setup the data storing file
    private val dataFile = "Data.txt"
    init {
        val defaultData = DataClass("2A-PINP", 0, 0)
        setupFile(dataFile, Json.encodeToString(defaultData))
        //editFile(dataFile, Json.encodeToString(defaultData))
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
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { it.write(content.toByteArray()) }
    }

    /**
     * Update the save file
     */
    fun updateSave() {
        context.openFileOutput(dataFile, Context.MODE_PRIVATE).use { it.write(Json.encodeToString(data).toByteArray()) }
    }

    /**
     * Opens a file and return its content, otherwise throws an error
     * @param filename Name of the file
     * @param test Whether or not a log should be sent
     */
    fun openFile(filename: String, test: Boolean = false): String {
        if (!test) {
            Log.v("FileManager", "Opened $filename")}; return File(context.filesDir, filename).readText()
    }

    /***
     * Creates a file with specified name and String-formatted content
     * @param filename Name of the file
     * @param content String-formatted content
     */
    private fun createFile(filename: String, content: String) {
        Log.v("FileManager", "Created $filename"); context.openFileOutput(filename, Context.MODE_PRIVATE).use { it.write(content.toByteArray()) }
    }

    fun getCurrentWeekId(): Int {
        return data.currentWeekId
    }

    companion object {
        var data: DataClass = DataClass("", 0, 0)
    }
}
