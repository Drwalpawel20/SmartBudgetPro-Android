package com.example.smartbudgetpro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbudgetpro.data.entity.Expense
import com.example.smartbudgetpro.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    // Najprostsza wersja – wszystkie wydatki
    fun getAllExpensesFlow(): Flow<List<Expense>> {

        return repository.getExpensesByDateRange(0L, System.currentTimeMillis() + 86400000L) // do jutra
    }

    // Jeśli chcesz wersję z callbackiem (jak miałeś wcześniej) – ale SAF lepiej obsłużyć w UI
    @Deprecated("Lepiej używać SAF w UI – patrz SettingsFragment")
    fun exportToCsv(start: Long, end: Long, onSuccess: (String) -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val expenses = repository.getExpensesByDateRange(start, end).first()
                // Tutaj NIE zapisuj pliku – zwróć dane i obsłuż w fragmencie
                // Jeśli bardzo chcesz – możesz tu zwrócić string CSV, ale nie ścieżkę pliku
                onSuccess("Eksport w nowej wersji odbywa się przez wybór lokalizacji przez użytkownika")
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Nieznany błąd")
            }
        }
    }
}