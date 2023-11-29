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

import android.util.Log
import android.view.MenuItem

class ClassHandler(val context: MainActivity, val dataHandler: DataHandler) {
    fun switchClass(name: String): Boolean {
        var search = ""
        var classeId = ""
        when (name) {

            /*
            * Prépa INP Valence
            */

            context.getString(R.string.PINPV_1A_name) -> {
                changeEcole("CPPV")
                classeId = context.getString(R.string.PINPV_1A)
            }

            context.getString(R.string.PINPV_2A_name) -> {
                changeEcole("CPPV")
                classeId = context.getString(R.string.PINPV_2A)
            }

            context.getString(R.string.PINPV_HN1_name) -> {
                changeEcole("CPPV")
                classeId = context.getString(R.string.PINPV_HN1)
            }

            context.getString(R.string.PINPV_HN2_name) -> {
                changeEcole("CPPV")
                classeId = context.getString(R.string.PINPV_HN2)
            }

            context.getString(R.string.PINPV_HN3_name) -> {
                changeEcole("CPPV")
                classeId = context.getString(R.string.PINPV_HN3)
            }

            /*
            * ESISAR
            */

            context.getString(R.string.ESISAR_1A_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_1A)
            }

            context.getString(R.string.ESISAR_2A_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_2A)
            }

            context.getString(R.string.ESISAR_3AS5_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_3AS5)
            }

            context.getString(R.string.ESISAR_3AS6EIS_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_3AS6EIS)
            }

            context.getString(R.string.ESISAR_3AS6IRC_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_3AS6IRC)
            }

            context.getString(R.string.ESISAR_3APP_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_3APP)
            }

            context.getString(R.string.ESISAR_4A_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_4A)
            }

            context.getString(R.string.ESISAR_4APP_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_4APP)
            }

            context.getString(R.string.ESISAR_5AEIS_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_5AEIS)
            }

            context.getString(R.string.ESISAR_5AIRC_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_5AIRC)
            }

            context.getString(R.string.ESISAR_5AMISTRE_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_5AMISTRE)
            }

            context.getString(R.string.ESISAR_5APP_name) -> {
                changeEcole("ESISAR")
                classeId = context.getString(R.string.ESISAR_5APP)
            }

        }
        if ((name != null) or (name != "") or (name != "null")) {
            Log.v("ClassHandler", "Changing class to $name with id $classeId")
            dataHandler.setTreeId(classeId)
            return true
        } else { return false }
    }

    /** Sauvegarde la nouvelle classe */
    private fun changeEcole(ecole: String) {
        dataHandler.setSchool(ecole)
    }
}