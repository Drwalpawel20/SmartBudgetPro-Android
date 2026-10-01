package com.example.smartbudgetpro.ui.stats

import androidx.lifecycle.ViewModel
import com.example.smartbudgetpro.data.entity.Category
import com.example.smartbudgetpro.data.entity.Expense
import com.example.smartbudgetpro.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<Expense>> {
        return repository.getExpensesByDateRange(start, end)
    }

    fun getCategoryExpenses(start: Long, end: Long): Flow<Map<Category, Double>> =
        repository.getCategoryExpenses(start, end)

    fun getDailyExpenses(start: Long, end: Long): Flow<Map<Long, Double>> =
        repository.getExpensesByDateRange(start, end)
            .map { expenses ->
                expenses.groupBy { expense ->
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = expense.date
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }.mapValues { (_, expenses) ->
                    expenses.sumOf { it.amount }
                }
            }

    fun getMonthlyTrendLastYear(): Flow<Map<Long, Double>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val start = calendar.timeInMillis

        return repository.getExpensesByDateRange(start, System.currentTimeMillis())
            .map { expenses ->
                expenses.groupBy { expense ->
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = expense.date
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }.mapValues { (_, expenses) ->
                    expenses.sumOf { it.amount }
                }.toSortedMap()
            }
    }
}