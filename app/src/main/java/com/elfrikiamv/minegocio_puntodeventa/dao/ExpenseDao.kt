package com.elfrikiamv.minegocio_puntodeventa.dao

// ExpenseDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.elfrikiamv.minegocio_puntodeventa.model.ExpenseEntity

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(ticket: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY expenseId DESC")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Query("DELETE FROM expenses WHERE expenseId = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

}