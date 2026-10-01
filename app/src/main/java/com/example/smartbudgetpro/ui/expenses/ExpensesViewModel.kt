package com.example.smartbudgetpro.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbudgetpro.data.entity.Category
import com.example.smartbudgetpro.data.entity.Expense
import com.example.smartbudgetpro.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    fun getAllExpenses(): Flow<List<Expense>> = repository.getAllExpenses()

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
        }
    }

    fun getAllCategories(): Flow<List<Category>> = repository.getAllCategories()

    fun initializeCategoriesIfEmpty() {
        viewModelScope.launch {
            repository.initializeCategoriesIfEmpty()
        }

    }
}