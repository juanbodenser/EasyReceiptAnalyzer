package com.easyreceiptanalyzer.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.easyreceiptanalyzer.data.AnalysisRepository
import com.easyreceiptanalyzer.data.MonthlyBalanceRepository
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.AnalysisDao
import com.easyreceiptanalyzer.data.local.AppDatabase
import com.easyreceiptanalyzer.data.local.MonthlyBalanceDao
import com.easyreceiptanalyzer.data.local.ReceiptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Migración de la versión 5 a la 6.
     * 
     * Cambios:
     * - Añade la columna `isExpense` a `receipts` (por defecto 1 = true = gasto).
     * - Crea la tabla `monthly_balances`.
     * 
     * Los datos existentes se conservan.
     */
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Añadir columna isExpense a receipts
            db.execSQL("ALTER TABLE receipts ADD COLUMN isExpense INTEGER NOT NULL DEFAULT 1")

            // 2. Crear tabla monthly_balances
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `monthly_balances` (
                    `yearMonth` TEXT NOT NULL,
                    `year` INTEGER NOT NULL,
                    `month` INTEGER NOT NULL,
                    `totalIncomeCents` INTEGER NOT NULL,
                    `totalExpenseCents` INTEGER NOT NULL,
                    `balanceCents` INTEGER NOT NULL,
                    `closedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`yearMonth`)
                )
            """)
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "easy_receipt_analyzer.db"
        )
            .addMigrations(MIGRATION_5_6)
            .build()
    }

    @Provides
    @Singleton
    fun provideReceiptDao(database: AppDatabase): ReceiptDao = database.receiptDao()

    @Provides
    @Singleton
    fun provideProductCategoryDao(database: AppDatabase): ProductCategoryDao =
        database.productCategoryDao()

    @Provides
    @Singleton
    fun provideAnalysisDao(database: AppDatabase): AnalysisDao = database.analysisDao()

    @Provides
    @Singleton
    fun provideMonthlyBalanceDao(database: AppDatabase): MonthlyBalanceDao =
        database.monthlyBalanceDao()

    @Provides
    @Singleton
    fun provideReceiptRepository(receiptDao: ReceiptDao): ReceiptRepository =
        ReceiptRepository(receiptDao)

    @Provides
    @Singleton
    fun provideAnalysisRepository(analysisDao: AnalysisDao): AnalysisRepository =
        AnalysisRepository(analysisDao)

    @Provides
    @Singleton
    fun provideMonthlyBalanceRepository(
        monthlyBalanceDao: MonthlyBalanceDao,
        analysisRepository: AnalysisRepository
    ): MonthlyBalanceRepository =
        MonthlyBalanceRepository(monthlyBalanceDao, analysisRepository)
}
