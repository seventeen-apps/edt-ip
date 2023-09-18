package com.seventeen.mywardrobe

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.seventeen.edtinp.MainActivity
import com.seventeen.edtinp.R
import com.seventeen.edtinp.loadImage
import java.util.*

class DatePicker(ctx: MainActivity) :
    DialogFragment(R.layout.date_dialog_fragment) {

    val ctx = ctx
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        val view: View = inflater!!.inflate(R.layout.date_picker_fragment, null)
        val datePickerView = view.findViewById<CalendarView>(R.id.date_picker)
        var strFormattedSelectedDate = ""
        var selectedDate : List<String> = listOf("")
        datePickerView.setOnDateChangeListener { _, year, month, day ->
            val c = Calendar.getInstance()
            c.set(year, month, day)
            val time = c.timeInMillis
            val selectedDayOfWeek = DateFormat.format("EEEE", Date(time)) as String
            val selectedMonthString = DateFormat.format("MMM", Date(time)) as String
            selectedDate = listOf(selectedDayOfWeek, day.toString(), selectedMonthString, month.toString(), year.toString())



            var selectedWeekNumber = c.get(Calendar.WEEK_OF_YEAR)
            if ((c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) or (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                selectedWeekNumber += 1
            }
            val selectedWeekId: Int
            if (selectedWeekNumber < 32) {
                selectedWeekId = selectedWeekNumber + 20
            } else {
                selectedWeekId = selectedWeekNumber - 32
            }


            strFormattedSelectedDate =
                "$selectedDayOfWeek, $selectedMonthString $day, $year"

            strFormattedSelectedDate = "$selectedWeekId"

            //TODO cleanup
            //TODO restreindre la sélection aux semaines valides

            /*Toast.makeText(
                inflater.context,
                "Selected Date = $strFormattedSelectedDate",
                Toast.LENGTH_SHORT
            ).show()*/
            //println(activity?.findViewById<TextView>(R.id.date_text)?.text)
            MainActivity().onDatePass(strFormattedSelectedDate, activity)
        }

        /*val button = view.findViewById<Button>(R.id.cancel_cloth_choice_button)
        button?.setOnClickListener {
            Log.v("DatePicker","Selected ${OutfitCreatorActivity.tempCloth["Date"]}")
            activity?.findViewById<TextView>(R.id.date_text)?.text = OutfitCreatorActivity.tempCloth["Date"]
            activity?.findViewById<TextView>(R.id.date_text)?.setTextColor(Color.BLACK)
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }*/

        val builder: AlertDialog.Builder = AlertDialog.Builder(layoutInflater.context)
        builder.setTitle("Choisissez une date")
        //builder.setMessage("Are you sure?")

        // Edited: Overriding onCreateView is not necessary in your case
        //val inflater = LayoutInflater.from(context)
        //val newFileView: View = inflater.inflate(R.layout.date_picker_fragment, null)
        builder.setView(view)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            // on success
            /*Log.v("DatePicker","Selected ${MainRecyclerViewActivity.tempOutfit["Date"]}")
            activity?.findViewById<TextView>(R.id.date_text)?.text = MainRecyclerViewActivity.tempOutfit["Date"]
            activity?.findViewById<TextView>(R.id.date_text)?.setTextColor(Color.BLACK)*/

            Log.v("DatePicker", MainActivity.selectedWeekId.toString())

            if (MainActivity.selectedWeekId != MainActivity.displayedWeekId) {
                MainActivity.displayedWeekId = MainActivity.selectedWeekId
                val imageWebView: WebView = ctx.findViewById(R.id.imageWebView)
                // Charge l'image dans la WebView principale
                val spliturl = MainActivity.referenceURL.split("&") as MutableList
                spliturl[MainActivity.idSemaineUrl] = "idPianoWeek=${MainActivity.selectedWeekId}"
                loadImage(imageWebView, spliturl.joinToString("&"))
            }

            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        })
        builder.setNegativeButton("Annuler",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

        return builder.create()
        //return view
        //return super.onCreateDialog(savedInstanceState)
    }

    interface OnDatePass {
        fun onDatePass(date: String, activity: FragmentActivity?)
    }

    companion object const

    val TAG = "DatePickerFragment"

}