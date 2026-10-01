package com.example.smartbudgetpro.ui.expenses

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudgetpro.R
import com.example.smartbudgetpro.data.entity.Expense
import com.example.smartbudgetpro.databinding.FragmentExpensesBinding
import com.example.smartbudgetpro.ui.adapter.ExpenseAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpensesViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExpenseAdapter { expense ->
            viewModel.deleteExpense(expense)
        }

        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        // Inicjalizacja domyślnych kategorii (jeśli baza pusta)
        lifecycleScope.launch {
            viewModel.initializeCategoriesIfEmpty()
        }

        // Pobieranie listy wydatków
        lifecycleScope.launch {
            viewModel.getAllExpenses().collect { expenses ->
                adapter.submitList(expenses)
            }
        }

        // Swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val expense = adapter.currentList[position]
                viewModel.deleteExpense(expense)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvExpenses)

        // FAB – dodawanie wydatku
        binding.fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }
    }

    // Dialog z wyborem kategorii
    private fun showAddExpenseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)

        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.et_amount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val etNote = dialogView.findViewById<TextInputEditText>(R.id.et_note)

        val calendar = Calendar.getInstance()
        val date = calendar.timeInMillis

        val spinnerAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nowy wydatek")
            .setView(dialogView)
            .setPositiveButton("Dodaj", null)
            .setNegativeButton("Anuluj", null)
            .show()

        val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false // Aktywny dopiero po załadowaniu kategorii

        lifecycleScope.launch {
            viewModel.getAllCategories().collect { categories ->
                if (categories.isEmpty()) {
                    spinnerAdapter.add("Brak kategorii")
                } else {
                    spinnerAdapter.clear()
                    categories.forEach { spinnerAdapter.add(it.name) }
                }
                spinnerAdapter.notifyDataSetChanged()
                positiveButton.isEnabled = true

                positiveButton.setOnClickListener {
                    val amountText = etAmount.text.toString().trim()
                    if (amountText.isNotEmpty() && categories.isNotEmpty()) {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val selectedPosition = spinnerCategory.selectedItemPosition
                        val categoryId = categories[selectedPosition].id
                        val note = etNote.text.toString().trim().takeIf { it.isNotBlank() }

                        val expense = Expense(
                            amount = amount,
                            categoryId = categoryId,
                            date = date,
                            note = note
                        )

                        viewModel.addExpense(expense)
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}