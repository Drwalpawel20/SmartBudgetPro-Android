package com.example.smartbudgetpro.di

import android.content.Context
import androidx.room.Room
import com.example.smartbudgetpro.data.AppDatabase
import com.example.smartbudgetpro.data.repository.FinanceRepository
import com.example.smartbudgetpro.utils.StatsCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finance_db"
        ).build()

    @Singleton
    @Provides
    fun provideFinanceRepository(db: AppDatabase): FinanceRepository =
        FinanceRepository(db, StatsCalculator())

    @Singleton
    @Provides
    fun provideStatsCalculator(): StatsCalculator = StatsCalculator()
}