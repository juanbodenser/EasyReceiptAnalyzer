package com.easyreceiptanalyzer.di

import android.content.Context
import androidx.room.Room
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ReceiptRepository
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideReceiptDao(database: AppDatabase): ReceiptDao {
        return database.receiptDao()
    }

    @Provides
    @Singleton
    fun provideProductCategoryDao(database: AppDatabase): ProductCategoryDao {
        return database.productCategoryDao()
    }

    @Provides
    @Singleton
    fun provideReceiptRepository(receiptDao: ReceiptDao): ReceiptRepository {
        return ReceiptRepository(receiptDao)
    }
}
