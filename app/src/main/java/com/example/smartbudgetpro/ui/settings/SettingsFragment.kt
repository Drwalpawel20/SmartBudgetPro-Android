package com.example.smartbudgetpro.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smartbudgetpro.LoginActivity
import com.example.smartbudgetpro.R
import com.example.smartbudgetpro.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    // Launcher do tworzenia pliku (użytkownik wybiera nazwę i miejsce)
    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                exportToCsvUsingUri(uri)
            } else {
                Toast.makeText(requireContext(), "Nie wybrano pliku", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Przycisk eksportu CSV
        binding.btnExportCsv.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE, "wydatki_smartbudget_${System.currentTimeMillis()}.csv")
            }
            createFileLauncher.launch(intent)
        }

        // Tryb ciemny / nocny
        val darkModeButton = view.findViewById<Button>(R.id.btn_dark_mode)
        darkModeButton.setOnClickListener {
            val isDarkMode = sharedPref.getBoolean("dark_mode", false)
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPref.edit().putBoolean("dark_mode", false).apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPref.edit().putBoolean("dark_mode", true).apply()
            }
            requireActivity().recreate()
        }

        // Wylogowanie
        val logoutButton = view.findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            sharedPref.edit().putBoolean("is_logged_in", false).apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
    }

// ... reszta importów bez zmian ...

    private fun exportToCsvUsingUri(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = viewModel.getAllExpensesFlow().first()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                requireContext().contentResolver.openOutputStream(uri)?.use { output ->
                    OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                        writer.write("ID;Kwota;Kategoria ID;Data;Notatka\n")  // ; zamiast , – lepsza kompatybilność z polskim Excelem

                        expenses.forEach { expense ->
                            val escapedNote = expense.note
                                ?.replace("\"", "\"\"")
                                ?.let { "\"$it\"" }
                                ?: ""

                            val amountFormatted =
                                String.format(Locale("pl", "PL"), "%.2f", expense.amount)

                            val line = listOf(
                                expense.id.toString(),
                                amountFormatted,
                                expense.categoryId.toString(),
                                dateFormat.format(expense.date),
                                escapedNote
                            ).joinToString(";")

                            writer.write("$line\n")
                        }
                    }
                }

                Toast.makeText(
                    requireContext(),
                    "Zapisano ${expenses.size} wydatków do CSV",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}