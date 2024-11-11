package com.example.indotinventario

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(val listener:(day:Int, month:Int, year:Int) -> Unit): DialogFragment(),
DatePickerDialog.OnDateSetListener{

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        listener(dayOfMonth, month, year)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar:Calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        return DatePickerDialog(activity as Context, this, year, month, day)
    }
}