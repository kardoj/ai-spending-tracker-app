package com.example.spendingtracker.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_NAME TEXT NOT NULL UNIQUE)"
        )
        // Seed initial categories
        db.execSQL("INSERT INTO $TABLE_NAME ($COLUMN_NAME) VALUES ('Food')")
        db.execSQL("INSERT INTO $TABLE_NAME ($COLUMN_NAME) VALUES ('Home')")

        // Create expenses table
        db.execSQL(
            "CREATE TABLE expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL," +
                    "category_id INTEGER NOT NULL," +
                    "timestamp INTEGER NOT NULL," +
                    "FOREIGN KEY(category_id) REFERENCES $TABLE_NAME($COLUMN_ID))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "spendingtracker.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "categories"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }
}