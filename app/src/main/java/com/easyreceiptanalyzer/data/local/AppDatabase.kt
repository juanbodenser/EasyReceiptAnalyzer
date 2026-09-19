package com.easyreceiptanalyzer.data.local

import android.content.Context
import androidx.room3.AndroidSQLiteDriver
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(entities = [ReceiptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}
