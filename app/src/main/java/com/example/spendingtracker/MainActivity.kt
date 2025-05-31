package com.example.spendingtracker

import Expense
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendingtracker.db.DbHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database and seed categories
        DbHelper(this).writableDatabase

        val rvExpenses = findViewById<RecyclerView>(R.id.rvExpenses)
        rvExpenses.layoutManager = LinearLayoutManager(this)

        adapter = ExpenseAdapter(loadExpenses())
        rvExpenses.adapter = adapter

        findViewById<FloatingActionButton>(R.id.btnTrack).setOnClickListener {
            startActivity(Intent(this, TrackExpenseActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list when returning to this activity
        val rvExpenses = findViewById<RecyclerView>(R.id.rvExpenses)
        adapter = ExpenseAdapter(loadExpenses())
        rvExpenses.adapter = adapter
    }

    private fun loadExpenses(): List<Expense> {
        val db = DbHelper(this).readableDatabase
        val expenses = mutableListOf<Expense>()
        val cursor = db.rawQuery(
            """
            SELECT e.id, e.amount, c.name, e.timestamp
            FROM expenses e
            JOIN categories c ON e.category_id = c.id
            ORDER BY e.timestamp DESC
            """.trimIndent(), null
        )
        while (cursor.moveToNext()) {
            expenses.add(
                Expense(
                    id = cursor.getLong(0),
                    amount = cursor.getDouble(1),
                    category = cursor.getString(2),
                    timestamp = cursor.getLong(3)
                )
            )
        }
        cursor.close()
        return expenses
    }
}