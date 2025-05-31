package com.example.spendingtracker.model

data class ExpenseEntity(
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val timestamp: Long
)