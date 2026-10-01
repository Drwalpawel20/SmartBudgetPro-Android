package com.example.smartbudgetpro.data.repository

import com.example.smartbudgetpro.R
import com.example.smartbudgetpro.data.AppDatabase
import com.example.smartbudgetpro.data.entity.Budget
import com.example.smartbudgetpro.data.entity.Category
import com.example.smartbudgetpro.data.entity.Expense
import com.example.smartbudgetpro.utils.StatsCalculator
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class FinanceRepository @Inject constructor(
    private val database: AppDatabase,
    private val statsCalculator: StatsCalculator
) {

    // Expenses
    suspend fun addExpense(expense: Expense) = database.expenseDao().insert(expense)
    suspend fun updateExpense(expense: Expense) = database.expenseDao().update(expense)
    suspend fun deleteExpense(expense: Expense) = database.expenseDao().delete(expense)

    fun getAllExpenses(): Flow<List<Expense>> = database.expenseDao().getAllExpenses()

    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<Expense>> =
        database.expenseDao().getExpensesByDateRange(start, end)

    fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Double> =
        database.expenseDao().getTotalExpensesByDateRange(start, end)
            .map { it ?: 0.0 }  // null → 0.0

    // Categories
    suspend fun addCategory(category: Category) = database.categoryDao().insert(category)

    fun getAllCategories(): Flow<List<Category>> = database.categoryDao().getAllCategories()

    // Budgets
    suspend fun addBudget(budget: Budget) = database.budgetDao().insert(budget)

    fun getBudgetForCategory(categoryId: Int, period: String): Flow<Budget?> =
        database.budgetDao().getBudgetForCategory(categoryId, period)

    fun getAllBudgets(): Flow<List<Budget>> = database.budgetDao().getAllBudgets()

    // Combined flows for stats
    fun getCategoryExpenses(monthStart: Long, monthEnd: Long): Flow<Map<Category, Double>> =
        combine(getAllCategories(), getExpensesByDateRange(monthStart, monthEnd)) { categories, expenses ->
            categories.associateWith { cat ->
                expenses.filter { it.categoryId == cat.id }.sumOf { it.amount }
            }
        }


    // Use StatsCalculator for insights
    fun calculateMonthlyInsights(
        currentMonthStart: Long,
        currentMonthEnd: Long,
        prevMonthStart: Long,
        prevMonthEnd: Long
    ): Flow<String> =
        combine(
            getTotalExpensesByDateRange(currentMonthStart, currentMonthEnd),
            getTotalExpensesByDateRange(prevMonthStart, prevMonthEnd),
            getCategoryExpenses(currentMonthStart, currentMonthEnd)
        ) { currentTotal, prevTotal, catExpenses ->
            statsCalculator.generateInsights(currentTotal, prevTotal, catExpenses)
        }

    // POPRAWIONA inicjalizacja kategorii – wszystko w tle, bez blokowania UI
    suspend fun initializeCategoriesIfEmpty() {
        withContext(Dispatchers.IO) {
            val categories = database.categoryDao().getAllCategories().first()
            if (categories.isEmpty()) {
                addCategory(Category(name = "Jedzenie", iconRes = R.drawable.ic_food))
                addCategory(Category(name = "Transport", iconRes = R.drawable.ic_transport))
                addCategory(Category(name = "Zakupy", iconRes = R.drawable.ic_shopping))
                addCategory(Category(name = "Rozrywka", iconRes = R.drawable.ic_entertainment))
                addCategory(Category(name = "Rachunki", iconRes = R.drawable.ic_bills))
                addCategory(Category(name = "Zdrowie", iconRes = R.drawable.ic_health))
                addCategory(Category(name = "Edukacja", iconRes = R.drawable.ic_education))
                addCategory(Category(name = "Inne", iconRes = R.drawable.ic_other))
            }
        }
    }
}