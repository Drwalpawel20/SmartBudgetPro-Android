package com.example.smartbudgetpro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartbudgetpro.data.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert
    suspend fun insert(budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budget WHERE categoryId = :categoryId AND period = :period")
    fun getBudgetForCategory(categoryId: Int, period: String): Flow<Budget?>

    @Query("SELECT * FROM budget")
    fun getAllBudgets(): Flow<List<Budget>>
}