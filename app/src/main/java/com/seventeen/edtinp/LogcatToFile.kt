package com.seventeen.edtinp

import android.content.Context
import android.util.Log
import com.seventeen.edtinp.deleteDir
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date

object LogcatToFile {
    fun saveLogcatToFile(context: Context, additionalLogDetail: String = "") {
        val folderName = "EDT logs"
        val logFolder =
            context.getExternalFilesDir(null).toString() + File.separator + folderName
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val logFileName =
            timeStamp + (if (additionalLogDetail != null) "_$additionalLogDetail" else "") + "log.txt"
        val logFilePath = logFolder + File.separator + logFileName


        Log.d("LogWriter", "Path: $logFolder")
        Log.d("LogWriter", "Date ${SimpleDateFormat("yyyyMMdd").format(Date())}")

        val directory = File(logFolder)
        val files = directory.listFiles()
        if (files != null) {
            Log.d("LogWriter", "Size: " + files.size)
        }
        if (files != null) {
            for (i in files.indices) {
                if (files.size > 30) {
                    if (SimpleDateFormat("yyyyMMdd").format(Date()).toString() !in files[i].name) {
                        files[i].delete()
                        Log.d("LogWriter", "FileName:" + files[i].name + " deleted")
                    }
                } else { Log.d("LogWriter", "FileName:" + files[i].name) }
            }
        }






        val folder = File(logFolder)
        if (!folder.exists()) {
            try {
                val mediaStorageDir = File(context.getExternalFilesDir(null), folderName)
                if (mediaStorageDir.mkdirs()) {
                    Log.v("LogWriter", "Log folder created successfully.")
                } else { Log.v("LogWriter", "Error when creating folder") }
            } catch (e: IOException) {
                Log.e("LogWriter", "Failed to create log folder : $e")
            }
        } else { Log.v("LogWriter", "Directory already created") }

        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String



            val folder = File(logFolder)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            val file = File(logFilePath)
            if (!file.exists()) {
                file.createNewFile()
            }

            val bufferedWriter = BufferedWriter(FileWriter(file, true))

            var working = true
            while (working) {

                var line = bufferedReader.readLine()
                if (line == null) {
                    working = false
                } else {
                    if (" W " !in line) {
                        // Handle line breaks or spaces
                        line = """
                                    $line
                                    
                                """.trimIndent()
                        bufferedWriter.write(line)
                    }
                }
            }

            bufferedWriter.close()
            Log.v("LogWriter", "Successfully saved log in $logFilePath")

        } catch (e: IOException) {
            Log.e("LogWriter", "Error saving logcat to file: " + e.message)
        }
    }
}