package com.easyreceiptanalyzer.di

import android.content.Context
import androidx.room3.AndroidSQLiteDriver
import androidx.room3.Room
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
        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = "easy_receipt_analyzer.db"
        )
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    @Provides
    @Singleton
    fun provideReceiptDao(database: AppDatabase): ReceiptDao {
        return database.receiptDao()
    }

    @Provides
    @Singleton
    fun provideReceiptRepository(receiptDao: ReceiptDao): ReceiptRepository {
        return ReceiptRepository(receiptDao)
    }
}