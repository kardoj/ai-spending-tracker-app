package com.example.spendingtracker

import android.text.InputFilter
import android.text.Spanned
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.spendingtracker.db.DbHelper
import java.text.SimpleDateFormat
import java.util.*

class TrackExpenseActivity : AppCompatActivity() {
    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var categoryIds: List<Long> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_expense)

        val etAmount = findViewById<EditText>(R.id.etAmount)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val btnPickDateTime = findViewById<Button>(R.id.btnPickDateTime)
        val tvDateTime = findViewById<TextView>(R.id.tvDateTime)
        val btnTrackExpense = findViewById<Button>(R.id.btnTrackExpense)

        // InputFilter to allow max two decimal places
        val filter = object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val result = StringBuilder(dest).replace(dstart, dend, source?.subSequence(start, end).toString())
                return if (Regex("^\\d*(\\.\\d{0,2})?\$").matches(result)) null else ""
            }
        }
        etAmount.filters = arrayOf(filter)

        // Set current date/time
        updateDateTimeText(tvDateTime, selectedTimestamp)

        btnPickDateTime.setOnClickListener {
            pickDateTime(tvDateTime)
        }

        // Load categories
        val db = DbHelper(this).readableDatabase
        val cursor = db.rawQuery("SELECT id, name FROM categories", null)
        val categories = mutableListOf<String>()
        categoryIds = mutableListOf()
        while (cursor.moveToNext()) {
            (categoryIds as MutableList<Long>).add(cursor.getLong(0))
            categories.add(cursor.getString(1))
        }
        cursor.close()
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        btnTrackExpense.setOnClickListener {
            var valid = true

            val amountStr = etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter a valid amount"
                valid = false
            }

            if (spinnerCategory.selectedItemPosition == AdapterView.INVALID_POSITION) {
                Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
                valid = false
            }

            if (valid) {
                val categoryId = categoryIds[spinnerCategory.selectedItemPosition]
                val dbw = DbHelper(this).writableDatabase
                dbw.execSQL(
                    "INSERT INTO expenses (amount, category_id, timestamp) VALUES (?, ?, ?)",
                    arrayOf(amount, categoryId, selectedTimestamp)
                )
                Toast.makeText(this, "Expense tracked!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun pickDateTime(tv: TextView) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedTimestamp

        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                selectedTimestamp = calendar.timeInMillis
                updateDateTimeText(tv, selectedTimestamp)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateTimeText(tv: TextView, timestamp: Long) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        tv.text = sdf.format(Date(timestamp))
    }
}