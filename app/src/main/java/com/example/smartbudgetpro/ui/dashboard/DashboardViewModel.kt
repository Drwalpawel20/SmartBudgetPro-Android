package com.example.smartbudgetpro.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.smartbudgetpro.data.entity.Category
import com.example.smartbudgetpro.data.entity.Expense
import com.example.smartbudgetpro.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    /* =============================
       SUMA WYDATKÓW
       ============================= */
    fun getTotalExpenses(start: Long, end: Long): Flow<Double> =
        repository.getExpensesByDateRange(start, end)
            .map { expenses ->
                expenses.sumOf { it.amount }
            }

    /* =============================
       PORÓWNANIE MIESIĘCY
       ============================= */
    fun getMonthlyInsights(
        currentStart: Long,
        currentEnd: Long,
        prevStart: Long,
        prevEnd: Long
    ): Flow<String> = combine(
        getTotalExpenses(currentStart, currentEnd),
        getTotalExpenses(prevStart, prevEnd)
    ) { current, prev ->
        if (prev == 0.0) {
            "Brak danych z poprzedniego miesiąca"
        } else {
            val diff = current - prev
            val percent = (diff / prev) * 100
            when {
                percent > 0 -> "Wydatki wzrosły o %.1f%%".format(percent)
                percent < 0 -> "Wydatki spadły o %.1f%%".format(abs(percent))
                else -> "Wydatki bez zmian"
            }
        }
    }

    /* =============================
       TOP 5 KATEGORII
       DAO: NIE RUSZAMY
       ============================= */
    fun getTopCategories(
        start: Long,
        end: Long
    ): Flow<List<Pair<String, Double>>> =
        combine(
            repository.getExpensesByDateRange(start, end),
            repository.getAllCategories()
        ) { expenses: List<Expense>, categories: List<Category> ->

            // mapowanie categoryId -> nazwa
            val categoryMap: Map<Int, String> =
                categories.associateBy({ it.id }, { it.name })

            // grupowanie wydatków po categoryId
            expenses
                .groupBy { it.categoryId }
                .mapNotNull { (categoryId, expenseList) ->
                    val name = categoryMap[categoryId] ?: return@mapNotNull null
                    name to expenseList.sumOf { it.amount }
                }
                .sortedByDescending { it.second }
                .take(5)
        }

    /* =============================
       PLACEHOLDER BUDŻET
       ============================= */
    val budgetExceeded: LiveData<Boolean> =
        repository.getAllExpenses()
            .map { false }
            .asLiveData()
}
