package com.seventeen.edtinp

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import java.util.Calendar

class DatePicker(private val imageHandler: ImageHandler, private val dataHandler: DataHandler) :
    DialogFragment(R.layout.date_dialog_fragment) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        val view: View = inflater!!.inflate(R.layout.date_picker_fragment, null)
        val datePickerView = view.findViewById<CalendarView>(R.id.date_picker)

        datePickerView.setOnDateChangeListener { _, year, month, day ->
            val c = Calendar.getInstance()
            c.set(year, month, day)

            var selectedWeekNumber = c.get(Calendar.WEEK_OF_YEAR)
            if ((c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) or (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                selectedWeekNumber += 1
            }
            val selectedWeekId: Int = if (selectedWeekNumber < 32) {
                selectedWeekNumber + 20
            } else {
                selectedWeekNumber - 32
            }
            //TODO restreindre la sélection aux semaines valides
            MainActivity().onDatePass(selectedWeekId, activity)
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(layoutInflater.context)
        builder.setTitle("Choisissez une date")
        builder.setView(view)

        builder.setPositiveButton("OK") { _, _ ->
            Log.v("DatePicker", MainActivity.selectedWeekId.toString())
            if (MainActivity.selectedWeekId != MainActivity.displayedWeekId) {
                MainActivity.displayedWeekId = MainActivity.selectedWeekId
                // Met à jour l'image
//                imageHandler.updateImage()
                imageHandler.updateWebView()
            }
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        builder.setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    /** Si utilisation en navigation restreinte, alors le bouton de validation est désactivé */
    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null && !MainActivity.navigationState) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
        }
    }

    interface OnDatePass {
        fun onDatePass(weekId: Int, activity: FragmentActivity?)
    }

    companion object Const {
        const val TAG = "DatePickerFragment"
    }

}