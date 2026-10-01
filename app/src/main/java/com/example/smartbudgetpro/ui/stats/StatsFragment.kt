package com.example.smartbudgetpro.ui.stats

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smartbudgetpro.R
import com.example.smartbudgetpro.databinding.FragmentStatsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    private var isMonthlyView = true
    private var monthlyLimit = 5000f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        monthlyLimit = prefs.getFloat("monthly_limit", 5000f)

        binding.segmentedButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isMonthlyView = checkedId == R.id.btn_monthly
                loadAllCharts()
            }
        }

        binding.segmentedButton.check(R.id.btn_monthly)

        binding.btnSetMonthlyLimit.setOnClickListener {
            showSetMonthlyLimitDialog()
        }

        loadAllCharts()
        loadDailyBalance()
    }

    private fun loadAllCharts() {
        val calendar = Calendar.getInstance()
        val (start, end) = if (isMonthlyView) {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val monthEnd = calendar.timeInMillis - 1
            monthStart to monthEnd
        } else {
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val yearStart = calendar.timeInMillis
            calendar.add(Calendar.YEAR, 1)
            val yearEnd = calendar.timeInMillis - 1
            yearStart to yearEnd
        }

        loadPieChart(start, end)
        loadBarChart(start, end)
        loadDailyTrendChart()
        loadLineChart()
    }

    private fun loadPieChart(start: Long, end: Long) {
        lifecycleScope.launch {
            viewModel.getCategoryExpenses(start, end).collect { catExpenses ->
                val entries = catExpenses.map { (category, amount) ->
                    PieEntry(amount.toFloat(), category.name)
                }

                val dataSet = PieDataSet(entries, "Kategorie").apply {
                    setColors(
                        Color.parseColor("#FF5722"), Color.parseColor("#4CAF50"),
                        Color.parseColor("#2196F3"), Color.parseColor("#FFC107"),
                        Color.parseColor("#9C27B0"), Color.parseColor("#00BCD4"),
                        Color.parseColor("#FF9800"), Color.parseColor("#8BC34A")
                    )
                    valueTextSize = 12f
                    valueTextColor = Color.WHITE
                }

                binding.pieChart.apply {
                    isDrawHoleEnabled = true

                    val surfaceColor = MaterialColors.getColor(
                        this, com.google.android.material.R.attr.colorSurface
                    )

                    setHoleColor(surfaceColor)
                    setTransparentCircleColor(surfaceColor)
                    setTransparentCircleAlpha(80)

                    setHoleRadius(45f)

                    data = PieData(dataSet)
                    description.isEnabled = false
                    legend.isEnabled = true
                    setEntryLabelTextSize(12f)
                    animateY(1000)
                    invalidate()
                }
            }
        }
    }

    private fun loadBarChart(start: Long, end: Long) {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val todayStart = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val weekStart = calendar.timeInMillis

        lifecycleScope.launch {

            viewModel.getExpensesByDateRange(weekStart, todayStart + 86400000).collect { expenses ->

                val dailyMap = mutableMapOf<Long, Float>()

                for (i in 0..6) {

                    val day = weekStart + i * 86400000
                    dailyMap[day] = 0f
                }

                expenses.forEach { exp ->

                    val cal = Calendar.getInstance()
                    cal.timeInMillis = exp.date

                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)

                    val day = cal.timeInMillis

                    dailyMap[day] = (dailyMap[day] ?: 0f) + exp.amount.toFloat()
                }

                val sortedDays = dailyMap.keys.sorted()

                val entries = sortedDays.mapIndexed { index, day ->
                    BarEntry(index.toFloat(), dailyMap[day] ?: 0f)
                }

                val labels = sortedDays.map { day ->
                    SimpleDateFormat("dd.MM", Locale.getDefault()).format(day)
                }

                val dataSet = BarDataSet(entries, "Wydatki (7 dni)").apply {

                    color = Color.parseColor("#2196F3")

                    valueTextSize = 10f
                    valueTextColor = Color.WHITE
                }

                binding.barChart.apply {

                    data = BarData(dataSet)

                    description.isEnabled = false

                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.granularity = 1f

                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false

                    legend.isEnabled = false

                    animateY(1000)

                    invalidate()
                }
            }
        }
    }

    private fun loadDailyTrendChart() {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayStart = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val dayEnd = calendar.timeInMillis - 1

        lifecycleScope.launch {

            viewModel.getExpensesByDateRange(dayStart, dayEnd).collect { expenses ->

                val entries = mutableListOf<Entry>()
                var sum = 0f

                expenses.sortedBy { it.date }.forEach { expense ->

                    sum += expense.amount.toFloat()

                    val minutesFromStart =
                        (expense.date - dayStart).toFloat() / (1000f * 60f)

                    entries.add(Entry(minutesFromStart, sum))
                }

                val dataSet = LineDataSet(entries, "Wydatki w czasie dnia").apply {

                    color = Color.parseColor("#4CAF50")
                    lineWidth = 3f

                    setDrawCircles(true)
                    setDrawValues(false)

                    mode = LineDataSet.Mode.STEPPED
                }

                binding.dailyTrendChart.apply {

                    data = LineData(dataSet)

                    description.text = ""

                    xAxis.position = XAxis.XAxisPosition.BOTTOM

                    xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {

                            val totalMinutes = value.toInt()

                            val hour = totalMinutes / 60
                            val minute = totalMinutes % 60

                            return String.format("%02d:%02d", hour, minute)
                        }
                    }

                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false

                    legend.isEnabled = false

                    animateX(1000)

                    invalidate()
                }
            }
        }
    }

    private fun loadLineChart() {
        lifecycleScope.launch {
            viewModel.getMonthlyTrendLastYear().collect { monthlyMap ->
                val entries = monthlyMap.keys.mapIndexed { idx, month ->
                    Entry(idx.toFloat(), monthlyMap[month]?.toFloat() ?: 0f)
                }

                val labels = monthlyMap.keys.map { date ->
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
                }

                val dataSet = LineDataSet(entries, "Trend miesięczny").apply {
                    color = Color.parseColor("#4CAF50")
                    valueTextSize = 10f
                    setCircleColor(Color.parseColor("#4CAF50"))
                    lineWidth = 2.5f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                binding.lineChart.apply {
                    data = LineData(dataSet)
                    description.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.granularity = 1f
                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    legend.isEnabled = true
                    animateX(1000)
                    invalidate()
                }
            }
        }
    }

    private fun loadDailyBalance() {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        val todayStart = now.timeInMillis
        now.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStart = now.timeInMillis - 1

        // Początek miesiąca
        val monthStartCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthStart = monthStartCal.timeInMillis

        lifecycleScope.launch {
            viewModel.getExpensesByDateRange(monthStart, tomorrowStart).collect { monthExpenses ->

                // Wydatki tylko do wczoraj (bez dzisiejszych)
                val pastDaysExpenses = monthExpenses.filter { it.date < todayStart }

                // Grupowanie po dniu
                val dailySpentMap = pastDaysExpenses
                    .groupBy { exp ->
                        Calendar.getInstance().apply {
                            timeInMillis = exp.date
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    }
                    .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }

                val todayDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val daysPassed = todayDayOfMonth - 1   // pełne dni przed dzisiaj

                val daysInMonth = monthStartCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val baseDailyLimit = monthlyLimit / daysInMonth

                val shouldHaveSpent = baseDailyLimit * daysPassed
                val actuallySpent = dailySpentMap.values.sum()

                val carryOver = shouldHaveSpent - actuallySpent   // + = niewydane, - = przekroczone

                val todayAvailableLimit = baseDailyLimit + carryOver

                // Dzisiejsze wydatki
                val todaySpent = monthExpenses
                    .filter { it.date in todayStart..tomorrowStart }
                    .sumOf { it.amount }
                    .toFloat()

                val currentDailyBalance = todayAvailableLimit - todaySpent

                binding.tvDailyBalance.apply {
                    text = if (currentDailyBalance >= 0) {
                        "Jesteś na plusie o ${currentDailyBalance.toInt()} zł"
                    } else {
                        "Przekroczyłeś o ${-currentDailyBalance.toInt()} zł"
                    }
                    setTextColor(if (currentDailyBalance >= 0) Color.GREEN else Color.RED)
                    visibility = View.VISIBLE
                }

                binding.tvTodaySpent.text = "Wydano dzisiaj: ${todaySpent.toInt()} zł"
                binding.tvDailyLimit.text = "Limit na dziś: ${todayAvailableLimit.toInt()} zł"
            }
        }
    }

    private fun showSetMonthlyLimitDialog() {
        val input = EditText(requireContext()).apply {
            setText(monthlyLimit.toInt().toString())
            hint = "Limit miesięczny (zł)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ustaw limit miesięczny")
            .setView(input)
            .setPositiveButton("Zapisz") { _, _ ->
                monthlyLimit = input.text.toString().toFloatOrNull() ?: 5000f
                requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putFloat("monthly_limit", monthlyLimit)
                    .apply()

                loadAllCharts()
                loadDailyBalance()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}