package com.example.smartbudgetpro.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudgetpro.databinding.ItemInvestmentBinding
import com.example.smartbudgetpro.ui.investment.InvestmentViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class InvestmentAdapter(
    private val onClick: (InvestmentViewModel.InvestmentItem) -> Unit
) : ListAdapter<InvestmentViewModel.InvestmentItem, InvestmentAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemInvestmentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InvestmentViewModel.InvestmentItem, onClick: (InvestmentViewModel.InvestmentItem) -> Unit) {
            binding.tvName.text = item.name
            binding.tvSymbol.text = item.symbol
            binding.tvPrice.text = "$ ${item.currentPrice}"
            binding.tvChange.text = "${item.change24h}%"
            binding.tvChange.setTextColor(if (item.change24h >= 0) Color.GREEN else Color.RED)

            // Mini-wykres trendu
            val entries = item.trendData.mapIndexed { index, price ->
                Entry(index.toFloat(), price)
            }

            val dataSet = LineDataSet(entries, "").apply {
                color = Color.parseColor("#4CAF50")
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
            }

            binding.trendChart.apply {
                data = LineData(dataSet)
                description.isEnabled = false
                xAxis.isEnabled = false
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                legend.isEnabled = false
                invalidate()
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvestmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<InvestmentViewModel.InvestmentItem>() {
        override fun areItemsTheSame(oldItem: InvestmentViewModel.InvestmentItem, newItem: InvestmentViewModel.InvestmentItem): Boolean = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: InvestmentViewModel.InvestmentItem, newItem: InvestmentViewModel.InvestmentItem): Boolean = oldItem == newItem
    }
}