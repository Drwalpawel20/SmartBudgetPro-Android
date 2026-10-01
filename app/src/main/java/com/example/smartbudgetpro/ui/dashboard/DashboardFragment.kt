package com.example.smartbudgetpro.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smartbudgetpro.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
        setupCalendarAndObserve()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setDrawEntryLabels(false)
            legend.isEnabled = true

            // ──── Kluczowe zmiany dla trybu nocnego ────────────────────────────────
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f  // lekko większy niż dziurka → delikatny pierścień

            // Pobieramy kolor powierzchni z aktualnego motywu Material 3
            val surfaceColor = MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorSurface
            )

            setHoleColor(surfaceColor)              // środek wykresu = kolor tła motywu
            setTransparentCircleColor(surfaceColor) // pierścień wokół dziurki
            setTransparentCircleAlpha(60)           // 0 = całkowicie przezroczysty, 60–110 = delikatny efekt

            // ──────────────────────────────────────────────────────────────────────
        }
    }

    private fun setupCalendarAndObserve() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentMonthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val currentMonthEnd = calendar.timeInMillis - 1

        calendar.add(Calendar.MONTH, -2)
        val prevMonthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val prevMonthEnd = calendar.timeInMillis - 1

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getTotalExpenses(currentMonthStart, currentMonthEnd)
                    .collectLatest { total ->
                        val value = total ?: 0.0
                        binding.tvTotalExpenses.text = "Wydatki: %.2f zł".format(value)
                        updateNetBalance(value)
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getMonthlyInsights(
                    currentMonthStart, currentMonthEnd,
                    prevMonthStart, prevMonthEnd
                ).collectLatest { binding.tvInsights.text = it }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getTopCategories(currentMonthStart, currentMonthEnd)
                    .collectLatest { updatePieChart(it) }
            }
        }
    }

    private fun updateNetBalance(expenses: Double) {
        val balance = -expenses
        binding.tvNetBalance.text = "Saldo netto: %.2f zł".format(balance)
        binding.tvNetBalance.setTextColor(
            if (balance >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
        )
    }

    private fun updatePieChart(categories: List<Pair<String, Double>>) {
        if (categories.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }

        binding.pieChart.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE

        val entries = categories.map { PieEntry(it.second.toFloat(), it.first) }

        val dataSet = PieDataSet(entries, "Top kategorie").apply {
            colors = pieColors()
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val data = PieData(dataSet)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "%.0f zł".format(value)
            }
        })

        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    private fun pieColors(): List<Int> = listOf(
        Color.parseColor("#4CAF50"),
        Color.parseColor("#2196F3"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#F44336"),
        Color.parseColor("#9C27B0")
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}