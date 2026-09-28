package com.easyreceiptanalyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.easyreceiptanalyzer.data.ProductCategoryDao
import com.easyreceiptanalyzer.data.ProductCategoryOverride

@Database(
    entities = [ReceiptEntity::class, ProductCategoryOverride::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun productCategoryDao(): ProductCategoryDao
}
