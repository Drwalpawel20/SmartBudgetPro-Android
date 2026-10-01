package com.example.smartbudgetpro.utils

import android.content.Context
import android.os.Environment
import com.example.smartbudgetpro.data.entity.Expense
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CsvExporter @Inject constructor(@ApplicationContext private val context: Context) {

    fun exportExpenses(expenses: List<Expense>): String {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "expenses_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { writer ->
            writer.append("ID,Amount,CategoryID,Date,Note\n")
            expenses.forEach { exp ->
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(exp.date))
                writer.append("${exp.id},${exp.amount},${exp.categoryId},$date,${exp.note ?: ""}\n")
            }
        }
        return file.absolutePath
    }
}