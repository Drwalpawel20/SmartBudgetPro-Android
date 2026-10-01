package com.example.smartbudgetpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartbudgetpro.R
import com.example.smartbudgetpro.data.dao.BudgetDao
import com.example.smartbudgetpro.data.dao.CategoryDao
import com.example.smartbudgetpro.data.dao.ExpenseDao
import com.example.smartbudgetpro.data.entity.Budget
import com.example.smartbudgetpro.data.entity.Category
import com.example.smartbudgetpro.data.entity.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Expense::class, Category::class, Budget::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Wstaw początkowe kategorie przy pierwszym uruchomieniu
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).categoryDao()
                                dao.insert(Category(name = "Jedzenie", iconRes = R.drawable.ic_food))
                                dao.insert(Category(name = "Transport", iconRes = R.drawable.ic_transport))
                                dao.insert(Category(name = "Zakupy", iconRes = R.drawable.ic_shopping))
                                dao.insert(Category(name = "Rozrywka", iconRes = R.drawable.ic_entertainment))
                                dao.insert(Category(name = "Rachunki", iconRes = R.drawable.ic_bills))
                                dao.insert(Category(name = "Zdrowie", iconRes = R.drawable.ic_health))
                                dao.insert(Category(name = "Edukacja", iconRes = R.drawable.ic_education))
                                dao.insert(Category(name = "Inne", iconRes = R.drawable.ic_other))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}