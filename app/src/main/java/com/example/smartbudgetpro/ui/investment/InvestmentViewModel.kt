package com.example.smartbudgetpro.ui.investment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbudgetpro.network.InvestmentApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val api: InvestmentApi
) : ViewModel() {

    private val _investments = MutableStateFlow<List<InvestmentItem>>(emptyList())
    val investments: StateFlow<List<InvestmentItem>> = _investments

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            try {
                val crypto = api.getTopCrypto()
                val items = crypto.map { crypto ->
                    InvestmentItem(
                        name = crypto.name,
                        symbol = crypto.symbol,
                        currentPrice = crypto.currentPrice,
                        change24h = crypto.change24h,
                        trendData = crypto.sparkline.price
                    )
                }
                _investments.value = items
            } catch (e: Exception) {
                // Błąd – pokaż komunikat w UI
            }
        }
    }

    data class InvestmentItem(
        val name: String,
        val symbol: String,
        val currentPrice: Float,
        val change24h: Float,
        val trendData: List<Float>
    )
}