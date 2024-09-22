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

            /*
             * ENSIMAG INGENIEURS
             */

            context.getString(R.string.ENSIMAG_INGENIEURS_1A_g1g2_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_1A_g1g2)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_1A_g3g4_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_1A_g3g4)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_1A_g5g6_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_1A_g5g6)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_1A_g7g8_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_1A_g7g8)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_IF_g1_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_IF_g1)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_IF_g2_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_IF_g2)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g1_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g1)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g2_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g2)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g3_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g3)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g4_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_ISI_g4)
            }


            context.getString(R.string.ENSIMAG_INGENIEURS_2A_MMIS_g1_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_MMIS_g1)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_MMIS_g2_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_MMIS_g2)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_2A_SEOC_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_2A_SEOC)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_3A_I2MF_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_3A_I2MF)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_3A_MEQA_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_3A_MEQA)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_3A_ISI_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_3A_ISI)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_3A_MMIS_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_3A_MMIS)
            }

            context.getString(R.string.ENSIMAG_INGENIEURS_3A_SEOC_name) -> {
                changeEcole("ENSIMAG Ingé")
                classeId = context.getString(R.string.ENSIMAG_INGENIEURS_3A_SEOC)
            }

            /*
             * ENSIMAG MASTERS
             */

            context.getString(R.string.ENSIMAG_MASTERS_CODAS1_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_CODAS1)
            }

            context.getString(R.string.ENSIMAG_MASTERS_CODAS2_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_CODAS2)
            }

            context.getString(R.string.ENSIMAG_MASTERS_CYSEC_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_CYSEC)
            }

            context.getString(R.string.ENSIMAG_MASTERS_MOSIG2_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_MOSIG2)
            }

            context.getString(R.string.ENSIMAG_MASTERS_MSIAM2_DS_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_MSIAM2_DS)
            }

            context.getString(R.string.ENSIMAG_MASTERS_MSIAM2_MSCI_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_MSIAM2_MSCI)
            }

            context.getString(R.string.ENSIMAG_MASTERS_RIE1_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_RIE1)
            }

            context.getString(R.string.ENSIMAG_MASTERS_RIE2_name) -> {
                changeEcole("ENSIMAG Masters")
                classeId = context.getString(R.string.ENSIMAG_MASTERS_RIE2)
            }

            /*
             * ENSE3
             */

            context.getString(R.string.ENSE3_1A_ALT_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_1A_ALT)
            }

            context.getString(R.string.ENSE3_2A_ALT_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ALT)
            }

            context.getString(R.string.ENSE3_3A_ALT_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_ALT)
            }

            context.getString(R.string.ENSE3_1A_ETU_A_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_1A_ETU_A)
            }

            context.getString(R.string.ENSE3_1A_ETU_B_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_1A_ETU_B)
            }

            context.getString(R.string.ENSE3_1A_ETU_C_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_1A_ETU_C)
            }

            context.getString(R.string.ENSE3_1A_ETU_3EUS_2APP_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_1A_ETU_3EUS_2APP)
            }

            context.getString(R.string.ENSE3_2A_ASI_G1_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ASI_G1)
            }

            context.getString(R.string.ENSE3_2A_ASI_G2_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ASI_G2)
            }

            context.getString(R.string.ENSE3_2A_ASI_G3_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ASI_G3)
            }

            context.getString(R.string.ENSE3_2A_ASI_G4_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ASI_G4)
            }

            context.getString(R.string.ENSE3_2A_HOE_S7_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_HOE_S7)
            }

            context.getString(R.string.ENSE3_2A_HOE_S8_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_HOE_S8)
            }

            context.getString(R.string.ENSE3_2A_IEE_G1_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEE_G1)
            }

            context.getString(R.string.ENSE3_2A_IEE_G2_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEE_G2)
            }

            context.getString(R.string.ENSE3_2A_IEE_G3_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEE_G3)
            }

            context.getString(R.string.ENSE3_2A_IEE_G4_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEE_G4)
            }

            context.getString(R.string.ENSE3_2A_IEN_G1_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEN_G1)
            }

            context.getString(R.string.ENSE3_2A_IEN_G2_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEN_G2)
            }

            context.getString(R.string.ENSE3_2A_IEN_ECOUL_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEN_ECOUL)
            }

            context.getString(R.string.ENSE3_2A_IEN_STRUCT_MAT_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_IEN_STRUCT_MAT)
            }

            context.getString(R.string.ENSE3_2A_ME_ETUD_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ME_ETUD)
            }

            context.getString(R.string.ENSE3_2A_ME_G1_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ME_G1)
            }

            context.getString(R.string.ENSE3_2A_ME_G2_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ME_G2)
            }

            context.getString(R.string.ENSE3_2A_ME_G3_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ME_G3)
            }

            context.getString(R.string.ENSE3_2A_ME_G4_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ME_G4)
            }

            context.getString(R.string.ENSE3_2A_ME_GEXTRATP_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ME_GEXTRATP)
            }

            context.getString(R.string.ENSE3_2A_ME_SIMFLU_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_ME_SIMFLU)
            }

            context.getString(R.string.ENSE3_2A_PARIN_ETU_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_PARIN_ETU)
            }

            context.getString(R.string.ENSE3_2A_PARCOURSNUM_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_PARCOURSNUM)
            }

            context.getString(R.string.ENSE3_2A_SEM_G1_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_SEM_G1)
            }

            context.getString(R.string.ENSE3_2A_SEM_G2_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_SEM_G2)
            }

            context.getString(R.string.ENSE3_2A_SEM_G3_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_SEM_G3)
            }

            context.getString(R.string.ENSE3_2A_SEM_G4_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_SEM_G4)
            }

            context.getString(R.string.ENSE3_2A_SICOM_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_2A_SICOM)
            }

            context.getString(R.string.ENSE3_3A_ML_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_ML)
            }

            context.getString(R.string.ENSE3_3A_ASI_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_ASI)
            }

            context.getString(R.string.ENSE3_3A_HOE_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_HOE)
            }

            context.getString(R.string.ENSE3_3A_IDP_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_IDP)
            }

            context.getString(R.string.ENSE3_3A_IEE_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_IEE)
            }

            context.getString(R.string.ENSE3_3A_IEN_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_IEN)
            }

            context.getString(R.string.ENSE3_3A_MANINTEC_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_MANINTEC)
            }

            context.getString(R.string.ENSE3_3A_ME_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_ME)
            }

            context.getString(R.string.ENSE3_3A_PARIN_ETU_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_PARIN_ETU)
            }

            context.getString(R.string.ENSE3_3A_PISTE_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_PISTE)
            }

            context.getString(R.string.ENSE3_3A_SEM_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_SEM)
            }

            context.getString(R.string.ENSE3_3A_SICOM_name) -> {
                changeEcole("ENSE3")
                classeId = context.getString(R.string.ENSE3_3A_SICOM)
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