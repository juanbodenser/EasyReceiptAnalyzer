package com.easyreceiptanalyzer.di

import android.content.Context
import androidx.room.Room
import com.easyreceiptanalyzer.data.AnalysisRepository
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ReceiptRepository
import com.easyreceiptanalyzer.data.local.AnalysisDao
import com.easyreceiptanalyzer.data.local.AppDatabase
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "easy_receipt_analyzer.db"
        )
            .fallbackToDestructiveMigration()
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
    fun provideReceiptRepository(receiptDao: ReceiptDao): ReceiptRepository =
        ReceiptRepository(receiptDao)

    @Provides
    @Singleton
    fun provideAnalysisRepository(analysisDao: AnalysisDao): AnalysisRepository =
        AnalysisRepository(analysisDao)
}
