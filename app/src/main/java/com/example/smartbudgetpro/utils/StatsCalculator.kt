package com.example.smartbudgetpro.utils

import com.example.smartbudgetpro.data.entity.Category
import javax.inject.Inject

class StatsCalculator @Inject constructor() {

    fun generateInsights(currentTotal: Double, prevTotal: Double, catExpenses: Map<Category, Double>): String {
        val percentageChange = if (prevTotal > 0) ((currentTotal - prevTotal) / prevTotal * 100) else 0.0
        val topCategory = catExpenses.maxByOrNull { it.value }?.key?.name ?: "Brak"
        return "Wydajesz o ${percentageChange.toInt()}% więcej niż w poprzednim miesiącu. Najwięcej pieniędzy idzie na: $topCategory"
    }

    // Other calculations for top 3, trends, etc.
}