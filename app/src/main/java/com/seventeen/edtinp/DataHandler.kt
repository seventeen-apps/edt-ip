package com.seventeen.edtinp

import android.content.Context
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class DataHandler {
    //Setup the data storing file
    private val dataFile = "Data.txt"
    fun setup(context: Context) {
        val defaultData = DataClass("2A-PINP")
        setupFile(dataFile, Json.encodeToString(defaultData), context)
        //editFile(dataFile, Json.encodeToString(defaultData))
        data = Json.decodeFromString<DataClass>(openFile(dataFile, context))
        Log.v("FileManager", "Loaded: ${Json.encodeToString(data)}")
    }

    /**
     * Find or create file if non-existent
     * @param filename Name of the file
     * @param content String-formatted content
     */
    private fun setupFile(filename: String, content: String, context: Context) {
        //Try to read the file
        try {
            openFile(filename, context, true)
            Log.v("FileManager", "$filename found")
        }
        //If file unreadable/unreachable, create a new one
        catch (e: Exception) {
            Log.v("FileManager", "$filename not found")
            createFile(filename, content, context)

        } finally {
            //Log.v("FileManager", "Setup terminated, $filename at ${getDir(filename, Context.MODE_PRIVATE)}")
        }
    }

    /**
     * Edit the file
     * @param filename Name of the file
     * @param content String-formatted content to write in the file
     */
    fun editFile(filename: String, content: String, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { it.write(content.toByteArray()) }
    }

    /**
     * Update the save file
     */
    fun updateSave(context: Context) {
        context.openFileOutput(dataFile, Context.MODE_PRIVATE).use { it.write(Json.encodeToString(data).toByteArray()) }
    }

    /**
     * Opens a file and return its content, otherwise throws an error
     * @param filename Name of the file
     * @param test Whether or not a log should be sent
     */
    fun openFile(filename: String, context: Context, test: Boolean = false): String {
        if (!test) {
            Log.v("FileManager", "Opened $filename")}; return File(context.filesDir, filename).readText()
    }

    /***
     * Creates a file with specified name and String-formatted content
     * @param filename Name of the file
     * @param content String-formatted content
     */
    private fun createFile(filename: String, content: String, context: Context) {
        Log.v("FileManager", "Created $filename"); context.openFileOutput(filename, Context.MODE_PRIVATE).use { it.write(content.toByteArray()) }
    }

    companion object {
        var data: DataClass = DataClass("")
    }
}
