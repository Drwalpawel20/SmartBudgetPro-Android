package com.example.smartbudgetpro.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudgetpro.data.entity.Expense
import com.example.smartbudgetpro.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    // Poprawny DiffUtil
    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }

    // ViewHolder z ViewBinding
    class ExpenseViewHolder(
        val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.tvAmount.text = "${expense.amount} zł"
            binding.tvNote.text = expense.note ?: "Brak notatki"
            binding.tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(expense.date))

            // TODO: Ustaw ikonę kategorii na podstawie expense.categoryId
            // binding.ivCategory.setImageResource(getCategoryIcon(expense.categoryId))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = getItem(position)  // getItem() dostępny dzięki ListAdapter
        holder.bind(expense)

        // Przykład swipe to delete – możesz rozbudować
        // holder.itemView.setOnClickListener { onDelete(expense) }
    }
}